from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import delete, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_session
from app.models import Habit
from app.schemas import CreateHabitRequest, HabitDto, UpdateHabitRequest

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


@router.get("/habits", response_model=list[HabitDto])
async def get_habits(session: AsyncSession = Depends(get_session)):
    result = await session.execute(
        select(Habit).order_by(Habit.created_at.desc())
    )
    return [_row_to_dto(row) for row in result.scalars().all()]


@router.get("/habits/{habit_id}", response_model=HabitDto)
async def get_habit(habit_id: str, session: AsyncSession = Depends(get_session)):
    result = await session.execute(
        select(Habit).where(Habit.id == habit_id)
    )
    row = result.scalar_one_or_none()
    if row is None:
        raise HTTPException(status_code=404, detail="Habit not found")
    return _row_to_dto(row)


@router.post("/habits", response_model=HabitDto, status_code=201)
async def create_habit(
    body: CreateHabitRequest,
    session: AsyncSession = Depends(get_session),
):
    habit = Habit(
        id=body.id,
        title=body.title,
        description=body.description,
        frequency=body.frequency,
        color_hex=body.colorHex,
        created_at=body.createdAt,
        last_completed_date=None,
    )
    session.add(habit)
    await session.commit()
    await session.refresh(habit)
    return _row_to_dto(habit)


@router.put("/habits/{habit_id}", response_model=HabitDto)
async def update_habit(
    habit_id: str,
    body: UpdateHabitRequest,
    session: AsyncSession = Depends(get_session),
):
    result = await session.execute(
        select(Habit).where(Habit.id == habit_id)
    )
    habit = result.scalar_one_or_none()
    if habit is None:
        raise HTTPException(status_code=404, detail="Habit not found")

    update_data = body.model_dump(exclude_unset=True)
    if "colorHex" in update_data:
        update_data["color_hex"] = update_data.pop("colorHex")
    if "lastCompletedDate" in update_data:
        update_data["last_completed_date"] = update_data.pop("lastCompletedDate")

    for key, value in update_data.items():
        setattr(habit, key, value)

    await session.commit()
    await session.refresh(habit)
    return _row_to_dto(habit)


@router.delete("/habits/{habit_id}", status_code=204)
async def delete_habit(
    habit_id: str,
    session: AsyncSession = Depends(get_session),
):
    await session.execute(delete(Habit).where(Habit.id == habit_id))
    await session.commit()
