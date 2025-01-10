package org.firstinspires.ftc.teamcode.opModes.auto

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.customHardware.AutoHardware
import org.firstinspires.ftc.teamcode.customHardware.AutoHardware.Companion.blueStartRight
import org.firstinspires.ftc.teamcode.customHardware.AutoHardware.Companion.redHuman
import org.firstinspires.ftc.teamcode.customHardware.AutoHardware.Companion.redSpecimen
import org.firstinspires.ftc.teamcode.customHardware.AutoHardware.Companion.redStartRight
import org.firstinspires.ftc.teamcode.customHardware.autoUtil.StartLocation
import org.firstinspires.ftc.teamcode.customHardware.autoUtil.startEnums.Alliance

//@Autonomous(group = GroupingTitles.auto) //@Disabled//disabling the opmode
class twoSpeci(val alliance: Alliance) : LinearOpMode() {
    override fun runOpMode() { //if opmode is started
        val robot = AutoHardware(
            this,
            StartLocation(
                this.alliance,
                if (alliance == Alliance.RED) redStartRight else blueStartRight,
            )
        )
//        var auto: StateMachine<out Enum<*>> = robot.smallSpecimenAutoR
//        if (alliance == Alliance.BLUE) {
//            auto = robot.smallSpecimenAutoB
//        }
        robot.autoSetup()
        robot.once()
        waitForStart()

        if (opModeIsActive()) {
            robot.scorePreloadSpeci()
            robot.grabSpeci(redSpecimen)
            robot.placeSpeci(-3)
            robot.endHuman(
                Pose2d(
                    redSpecimen.position.x - 3,
                    redHuman.position.y + 25,
                    redSpecimen.heading.toDouble()
                )
            )
        }
    }
}
