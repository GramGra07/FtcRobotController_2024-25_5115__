package org.firstinspires.ftc.teamcode.subsystems

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import com.arcrobotics.ftclib.command.CommandBase
import com.arcrobotics.ftclib.command.SubsystemBase
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.UtilClass.ServoUtil
import org.firstinspires.ftc.teamcode.UtilClass.varStorage.AutoServoPositions
import org.firstinspires.ftc.teamcode.UtilClass.varStorage.PotentPositions
import org.firstinspires.ftc.teamcode.extensions.SensorExtensions.potentAngle
import org.firstinspires.ftc.teamcode.extensions.ServoExtensions
import org.firstinspires.ftc.teamcode.extensions.ServoExtensions.calcFlipPose
import org.firstinspires.ftc.teamcode.extensions.ServoExtensions.initServo
import org.firstinspires.ftc.teamcode.opModes.HardwareConfig.Companion.clawSubsystem
import org.firstinspires.ftc.teamcode.opModes.HardwareConfig.Companion.potentiometer


class ClawSubsystem(ahwMap: HardwareMap) : SubsystemBase() {
    enum class ClawStates {
        OPEN,
        CLOSED
    }

    enum class FlipStates {
        HIGH,
        BACK,
        ZERO,
        UP,
        DOWN
    }

    private var claw1: Servo
    private var claw2: Servo
    private var flipServo: Servo

    init {
        claw1 = initServo(ahwMap, "claw1")
        claw2 = initServo(ahwMap, "claw2")
        flipServo = initServo(ahwMap, "flipServo")
        closeBoth()
        flipBack()
        update()
    }

    class clawDefault : CommandBase() {
        private fun update() {
            this.update()
        }

        override fun execute() {
            update()
        }
    }

    private var rightState = ClawStates.CLOSED
    private var leftState = ClawStates.CLOSED
    private var flipState = FlipStates.BACK

    private fun openBoth() {
        rightState = ClawStates.OPEN
        leftState = ClawStates.OPEN
    }

    private fun openLeft() {
        leftState = ClawStates.OPEN
    }

    private fun openRight() {
        rightState = ClawStates.OPEN
    }

    private fun closeBoth() {
        leftState = ClawStates.CLOSED
        rightState = ClawStates.CLOSED
    }

    private fun closeLeft() {
        leftState = ClawStates.CLOSED
    }

    private fun closeRight() {
        rightState = ClawStates.CLOSED
    }

    private fun flipHigh() {
        flipState = FlipStates.HIGH
    }

    private fun flipUp() {
        flipState = FlipStates.UP
    }

    private fun flipBack() {
        flipState = FlipStates.BACK
    }

    private fun flipDown() {
        flipState = FlipStates.DOWN
    }

    private fun flipZero() {
        flipState = FlipStates.ZERO
    }
    private class CloseBoth : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.openBoth()
            return false
        }
    }

    fun closeBothAction(): Action {
        return CloseBoth()
    }

    private class OpenBoth : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.openBoth()
            return false
        }
    }

    fun openBothAction(): Action {
        return OpenBoth()
    }

    private class OpenLeft : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.openLeft()
            return false
        }
    }

    fun openLeftAction(): Action {
        return OpenLeft()
    }

    private class OpenRight : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.openRight()
            return false
        }
    }

    fun openRightAction(): Action {
        return OpenRight()
    }

    private class CloseLeft : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.closeLeft()
            return false
        }
    }

    fun closeLeftAction(): Action {
        return CloseLeft()
    }

    private class CloseRight : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.closeRight()
            return false
        }
    }

    fun closeRightAction(): Action {
        return CloseRight()
    }

    private class FlipHigh : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.flipHigh()
            return false
        }
    }

    fun flipHighAction(): Action {
        return FlipHigh()
    }

    private class FlipUp : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.flipUp()
            return false
        }
    }

    fun flipUpAction(): Action {
        return FlipUp()
    }

    private class FlipBack : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.flipBack()
            return false
        }
    }

    fun flipBackAction(): Action {
        return FlipBack()
    }

    private class FlipDown : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.flipDown()
            return false
        }
    }

    fun flipDownAction(): Action {
        return FlipDown()
    }

    private class FlipZero : Action {
        override fun run(p: TelemetryPacket): Boolean {
            clawSubsystem.flipZero()
            return false
        }
    }

    fun flipZeroAction(): Action {
        return FlipZero()
    }

    fun update() {
        when (rightState) {
            ClawStates.OPEN -> ServoUtil.openClaw1(claw1)
            ClawStates.CLOSED -> ServoUtil.closeClaw1(claw1)
        }
        when (leftState) {
            ClawStates.OPEN -> ServoUtil.openClaw2(claw2)
            ClawStates.CLOSED -> ServoUtil.closeClaw2(claw2)
        }
        when (flipState) {
            FlipStates.HIGH -> flipServo.calcFlipPose(70.0)
            FlipStates.BACK -> flipServo.calcFlipPose(ServoUtil.backClaw.toDouble())
            FlipStates.ZERO -> flipServo.calcFlipPose(0.0)
            FlipStates.UP -> flipServo.calcFlipPose(AutoServoPositions.flipUp.toDouble())
            FlipStates.DOWN -> flipServo.calcFlipPose((AutoServoPositions.flipDown - 10).toDouble())
        }
        if (PotentPositions.pastAngleVal != potentiometer.potentAngle()) {
            flipServo.calcFlipPose(ServoExtensions.lastSetVal.toDouble())
        }
    }
}