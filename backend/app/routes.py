from fastapi import APIRouter, Depends
from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_session
from app.models import Habit
from app.schemas import HabitDto, SyncHabitsRequest, SyncHabitsResponse

router = APIRouter()


def _row_to_dto(row: Habit) -> HabitDto:
    return HabitDto(
        id=row.id,
        title=row.title,
        description=row.description,
        frequency=row.frequency,
        colorHex=row.color_hex,
        createdAt=row.created_at,
        lastCompletedDate=row.last_completed_date,
    )


def _dto_to_model(dto: HabitDto) -> Habit:
    return Habit(
        id=dto.id,
        title=dto.title,
        description=dto.description,
        frequency=dto.frequency,
        color_hex=dto.colorHex,
        created_at=dto.createdAt,
        last_completed_date=dto.lastCompletedDate,
    )


@router.get("/habits", response_model=list[HabitDto])
async def get_habits(session: AsyncSession = Depends(get_session)):
    result = await session.execute(
        select(Habit).order_by(Habit.created_at.desc())
    )
    return [_row_to_dto(row) for row in result.scalars().all()]


@router.post("/habits/sync", response_model=SyncHabitsResponse)
async def sync_habits(
    body: SyncHabitsRequest,
    session: AsyncSession = Depends(get_session),
):
    for dto in body.habits:
        model = _dto_to_model(dto)
        await session.merge(model)
    await session.commit()
    return SyncHabitsResponse(uploadedCount=len(body.habits))


@router.delete("/habits/{habit_id}")
async def delete_habit(
    habit_id: str,
    session: AsyncSession = Depends(get_session),
):
    await session.execute(delete(Habit).where(Habit.id == habit_id))
    await session.commit()
    return {"deleted": True}
