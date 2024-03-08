package org.firstinspires.ftc.teamcode.opModes.auto.pixelParkAutos.endIn.outsidePath

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.Enums.Alliance
import org.firstinspires.ftc.teamcode.Enums.EndPose
import org.firstinspires.ftc.teamcode.Enums.PathLong
import org.firstinspires.ftc.teamcode.Enums.StartSide
import org.firstinspires.ftc.teamcode.opModes.HardwareConfig.Companion.clawSubsystem
import org.firstinspires.ftc.teamcode.opModes.HardwareConfig.Companion.driveSubsystem
import org.firstinspires.ftc.teamcode.opModes.HardwareConfig.Companion.extendoSubsystem
import org.firstinspires.ftc.teamcode.opModes.autoSoftware.AutoHardware
import org.firstinspires.ftc.teamcode.opModes.autoSoftware.AutoHardware.Companion.getStartPose
import org.firstinspires.ftc.teamcode.opModes.autoSoftware.autoPatterns.pixelParkMachine
import org.firstinspires.ftc.teamcode.opModes.autoSoftware.autoPatterns.pixelParkStates
import org.firstinspires.ftc.teamcode.opModes.autoSoftware.autoSorting.piParkOSort
import org.firstinspires.ftc.teamcode.opModes.autoSoftware.autoSorting.preselect
import org.gentrifiedApps.statemachineftc.StateMachine

@Autonomous(group = piParkOSort, preselectTeleOp = preselect) //@Disabled

class PiParkRLOpI : LinearOpMode() {
    var startPose = AutoHardware.startPose
    var robot = AutoHardware(this, hardwareMap, true)
    override fun runOpMode() {
        driveSubsystem!!.drive!!.poseEstimate = getStartPose(Alliance.RED, StartSide.LEFT)
        val machine: StateMachine<pixelParkStates> =
            pixelParkMachine(
                PathLong.OUTSIDE, EndPose.LEFT,
                clawSubsystem!!,
                extendoSubsystem!!,
                driveSubsystem!!
            )
        machine.start()
        while (machine.mainLoop(this)) {
            machine.update()
        }
    }
}