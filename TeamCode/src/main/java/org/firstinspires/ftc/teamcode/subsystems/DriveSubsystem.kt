package org.firstinspires.ftc.teamcode.subsystems

import CancelableFollowTrajectoryAction
import com.acmerobotics.roadrunner.Action
import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.util.Range
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.teamcode.customHardware.HardwareConfig
import org.firstinspires.ftc.teamcode.extensions.MotorExtensions.initMotor
import org.firstinspires.ftc.teamcode.rr.MecanumDrive
import org.firstinspires.ftc.teamcode.storage.CurrentDrivetrain
import org.firstinspires.ftc.teamcode.storage.DistanceStorage
import org.firstinspires.ftc.teamcode.storage.PoseStorage
import org.firstinspires.ftc.teamcode.subsystems.avoidance.AvoidanceSubsystem
import org.firstinspires.ftc.teamcode.subsystems.humanInput.Drivers
import org.firstinspires.ftc.teamcode.utilClass.FileWriterFTC
import org.firstinspires.ftc.teamcode.utilClass.drivetrain.DrivetrainType
import org.firstinspires.ftc.teamcode.utilClass.objects.DriveType
import org.firstinspires.ftc.teamcode.utilClass.varConfigurations.varConfig
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt


//@Config
class DriveSubsystem(ahwMap: HardwareMap) {
    var drive: MecanumDrive

    private var motorFrontLeft: DcMotorEx
    private var motorBackLeft: DcMotorEx
    private var motorFrontRight: DcMotorEx
    private var motorBackRight: DcMotorEx

    private var currentSpeed: Double = 0.0

    //    var odometrySubsystem: OdometrySubsystem3Wheel? = null
    lateinit var cancelableFollowing: CancelableFollowTrajectoryAction

    init {
//        if (odometrySubsystem == null) {
//            odometrySubsystem = OdometrySubsystem3Wheel(ahwMap, 0.0, 0.0, 0.0)
//        }
        drive = MecanumDrive(ahwMap, Pose2d(0.0, 0.0, 0.0))

        motorFrontLeft =
            initMotor(
                ahwMap,
                "motorFrontLeft",
                DcMotor.RunMode.RUN_WITHOUT_ENCODER
            )
        motorBackLeft = initMotor(
            ahwMap,
            "motorBackLeft",
            DcMotor.RunMode.RUN_WITHOUT_ENCODER,
        )
        motorFrontRight =
            initMotor(
                ahwMap,
                "motorFrontRight",
                DcMotor.RunMode.RUN_WITHOUT_ENCODER
            )
        motorBackRight =
            initMotor(
                ahwMap,
                "motorBackRight",
                DcMotor.RunMode.RUN_WITHOUT_ENCODER
            )
        motorBackLeft.direction = DcMotorSimple.Direction.REVERSE

        reset()
    }

    private var thisDist = 0.0
    private var lastTime = 0.0
    private var frontRightPower = 0.0
    private var frontLeftPower = 0.0
    private var backRightPower = 0.0
    private var backLeftPower = 0.0
    var slowModeIsOn = false
    private var reverse = false
    var isAutoInTeleop = false
    var goZero: Action? = null
    var leftStickX = 0.0
    var leftStickY = 0.0
    var rightStickX = 0.0
    fun driveByGamepads(type: DriveType, myOpMode: OpMode, timer: Double) {
        val drivetrain = CurrentDrivetrain.currentDrivetrain
        // Retrieve gamepad values
        leftStickX = myOpMode.gamepad1.left_stick_x.toDouble()
        leftStickY = -myOpMode.gamepad1.left_stick_y.toDouble()
        rightStickX = -myOpMode.gamepad1.right_stick_x.toDouble()

        val slowPower = if (slowModeIsOn) varConfig.slowMult else 1
//
        if (drivetrain.type == DrivetrainType.MECANUM) {
            if (type == DriveType.FIELD_CENTRIC) {
                val controllerAngle = Math.toDegrees(atan2(leftStickY, leftStickX))
                val robotDegree = Math.toDegrees(drive.pose.heading.toDouble())
                val movementDegree = controllerAngle - robotDegree
                val gamepadHypot = Range.clip(hypot(leftStickX, leftStickY), 0.0, 1.0)

                // Compute x and y controls
                val xControl = cos(Math.toRadians(movementDegree)) * gamepadHypot
                val yControl = sin(Math.toRadians(movementDegree)) * gamepadHypot

                // Compute powers
                val turn = rightStickX
                frontRightPower =
                    (yControl * abs(yControl) - xControl * abs(xControl) + turn) / slowPower
                backRightPower =
                    (yControl * abs(yControl) + xControl * abs(xControl) + turn) / slowPower
                frontLeftPower =
                    (yControl * abs(yControl) + xControl * abs(xControl) - turn) / slowPower
                backLeftPower =
                    (yControl * abs(yControl) - xControl * abs(xControl) - turn) / slowPower
            } else if (type == DriveType.ROBOT_CENTRIC) {
                val turn = rightStickX
                frontRightPower =
                    (leftStickY * abs(leftStickY) - leftStickX * abs(leftStickX) + turn) / slowPower
                backRightPower =
                    (leftStickY * abs(leftStickY) + leftStickX * abs(leftStickX) + turn) / slowPower
                frontLeftPower =
                    (leftStickY * abs(leftStickY) + leftStickX * abs(leftStickX) - turn) / slowPower
                backLeftPower =
                    (leftStickY * abs(leftStickY) - leftStickX * abs(leftStickX) - turn) / slowPower
            }
        } else if (drivetrain.type == DrivetrainType.TANK) {
            frontLeftPower = Range.clip(leftStickY + rightStickX, -1.0, 1.0)
            backLeftPower = frontLeftPower
            frontRightPower = Range.clip(leftStickY - rightStickX, -1.0, 1.0)
            backRightPower = frontRightPower
        }

        // Update distance traveled
        updateDistTraveled(PoseStorage.currentPose, drive.pose, timer)
        FileWriterFTC.writeToFile(
            HardwareConfig.fileWriter,
            drive.pose.position.x.toInt(),
            drive.pose.position.y.toInt()
        )

        PoseStorage.currentPose = drive.pose
    }


    private fun updateDistTraveled(before: Pose2d, after: Pose2d, timer: Double) {
        val deltaX = after.position.x - before.position.x
        val deltaY = after.position.y - before.position.y
        val dist = sqrt(deltaX * deltaX + deltaY * deltaY)
        val deltaTime = timer - lastTime
        lastTime = timer
        currentSpeed = (dist / deltaTime) * 0.0568
        thisDist += dist
        DistanceStorage.totalDist += dist
    }

    private fun reset() {
        thisDist = 0.0
    }

    private fun power(avoidanceSubsystem: AvoidanceSubsystem) {
        if (!isAutoInTeleop) {
            var flP = 0.0
            var frP = 0.0
            var rrP = 0.0
            var rlP = 0.0
            if (avoidanceSubsystem.powers != null) {
                val addedPowers: Map<String, Double?>? = avoidanceSubsystem.powers
                flP = addedPowers?.getOrDefault("FL", 0.0) ?: 0.0
                frP = addedPowers?.getOrDefault("FR", 0.0) ?: 0.0
                rlP = addedPowers?.getOrDefault("RL", 0.0) ?: 0.0
                rrP = addedPowers?.getOrDefault("RR", 0.0) ?: 0.0
            }

            frontLeftPower = Range.clip(frontLeftPower + flP, -1.0, 1.0)
            frontRightPower = Range.clip(frontRightPower + frP, -1.0, 1.0)
            backLeftPower = Range.clip(backLeftPower + rlP, -1.0, 1.0)
            backRightPower = Range.clip(backRightPower + rrP, -1.0, 1.0)

            motorFrontLeft.power = frontLeftPower
            motorBackLeft.power = backLeftPower
            motorFrontRight.power = frontRightPower
            motorBackRight.power = backRightPower
        }
    }


    fun update(
        avoidanceSubsystem: AvoidanceSubsystem,
        type: AvoidanceSubsystem.AvoidanceTypes
    ) {
        avoidanceSubsystem.update(this, type)
        power(avoidanceSubsystem)
//        odometrySubsystem!!.update()
    }

    fun telemetry(telemetry: Telemetry) {
        val drivetrain = CurrentDrivetrain.currentDrivetrain
        drivetrain.telemetry(telemetry)
        if (reverse) {
            telemetry.addData("reversed", "")
        }
        if (slowModeIsOn) {
            telemetry.addData("slowMode", "")
        }
        when (Drivers.currentFieldCentric) {
            DriveType.FIELD_CENTRIC -> telemetry.addData("fieldCentric", "")
            DriveType.ROBOT_CENTRIC -> telemetry.addData("robotCentric", "")
        }
        telemetry.addData("totalDistance (in)", "%.1f", DistanceStorage.totalDist)
        telemetry.addData("Current Speed (mph)", "%.1f", currentSpeed)
//        odometrySubsystem!!.telemetry(telemetry)
    }
}