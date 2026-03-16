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


class CreateHabitRequest(BaseModel):
    id: str
    title: str
    description: str = ""
    frequency: str = "DAILY"
    colorHex: str = "#6750A4"
    createdAt: int


class UpdateHabitRequest(BaseModel):
    title: str | None = None
    description: str | None = None
    frequency: str | None = None
    colorHex: str | None = None
    lastCompletedDate: str | None = None
