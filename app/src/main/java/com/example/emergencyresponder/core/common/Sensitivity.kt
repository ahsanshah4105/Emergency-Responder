enum class Sensitivity(
    val crashAccel: Double,
    val snatchAccel: Double,
    val gyro: Double,
    val minScore: Int
) {
    // LOW: Only triggers on a literal drop to a hard floor or car impact
    LOW(60.0, 45.0, 15.0, 1000),

    // MEDIUM: Standard "hard shake" or fall
    MEDIUM(35.0, 25.0, 8.0, 850),

    // HIGH: Very easy to trigger; a quick hand flick will do it
    HIGH(12.0, 8.0, 3.0, 700)
}