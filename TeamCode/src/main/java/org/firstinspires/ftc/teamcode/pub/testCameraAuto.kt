package org.firstinspires.ftc.teamcode.pub

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.teamcode.UtilClass.camUtil.Processor
import org.firstinspires.ftc.teamcode.opModes.autoSoftware.AutoHardware
import org.firstinspires.ftc.teamcode.startEnums.Alliance
import org.firstinspires.ftc.teamcode.startEnums.StartSide

@Autonomous
class testCameraAuto : LinearOpMode() {
    private lateinit var robot: AutoHardware
    override fun runOpMode() {
        robot = AutoHardware(this, hardwareMap, Processor.PUB_TEST, Alliance.RED, StartSide.LEFT)
    }
}