from sqlalchemy import BigInteger, Column, String
from sqlalchemy.orm import DeclarativeBase


class Base(DeclarativeBase):
    pass


class Habit(Base):
    __tablename__ = "habits"

    id = Column(String, primary_key=True)
    title = Column(String, nullable=False)
    description = Column(String, nullable=False, default="")
    frequency = Column(String, nullable=False, default="DAILY")
    color_hex = Column(String, nullable=False, default="#6750A4")
    created_at = Column(BigInteger, nullable=False)
    last_completed_date = Column(String, nullable=True)
