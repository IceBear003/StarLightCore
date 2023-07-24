package world.icebear03.starlight.career.internal

enum class SkillType(val typeName: String) {
    PASSIVE("被动"),
    ACTIVE("主动"),
    ACTIVE_ONLINE("主动 (仅限在线时读冷却)")
}