package org.firstinspires.ftc.teamcode.opModes.teleOp

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.opModes.HardwareConfig
import org.firstinspires.ftc.teamcode.subsystems.ClawSubsystem
import org.firstinspires.ftc.teamcode.subsystems.DriveSubsystem
import org.firstinspires.ftc.teamcode.subsystems.EndgameSubsystem
import org.firstinspires.ftc.teamcode.subsystems.ExtendoSubsystem

@TeleOp(group = "a") //@Disabled//disabling the opmode

class teleOp : LinearOpMode() {
    //declaring the class
    var robot = HardwareConfig(this)
    override fun runOpMode() { //if opmode is started
        HardwareConfig.init(hardwareMap, false)
        waitForStart()
        HardwareConfig.timer.reset()
        while (opModeIsActive()) { //while the op mode is active
            robot.doBulk()
        }
    }
}