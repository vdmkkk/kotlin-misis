from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    database_url: str = "postgresql+asyncpg://habits:habits@db:5432/habits"

    model_config = {"env_prefix": "APP_"}


settings = Settings()
