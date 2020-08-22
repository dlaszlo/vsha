package hu.dlaszlo.vsha.backend.device

enum class ScheduleType {
    Immediate,
    Timeout,
    FixedRate,
    CronScheduler
}