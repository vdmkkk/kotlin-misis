from pydantic import BaseModel, ConfigDict


class HabitDto(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    id: str
    title: str
    description: str
    frequency: str
    colorHex: str
    createdAt: int
    lastCompletedDate: str | None = None


class SyncHabitsRequest(BaseModel):
    habits: list[HabitDto]


class SyncHabitsResponse(BaseModel):
    uploadedCount: int
