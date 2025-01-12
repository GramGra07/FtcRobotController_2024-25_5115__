package org.firstinspires.ftc.teamcode.subsystems

import com.acmerobotics.dashboard.telemetry.TelemetryPacket
import com.acmerobotics.roadrunner.Action
import org.firstinspires.ftc.teamcode.subsystems.gameSpecific.ArmSubsystem
import org.firstinspires.ftc.teamcode.subsystems.gameSpecific.ScoringSubsystem
import org.firstinspires.ftc.teamcode.utilClass.varConfigurations.DAVars
import org.firstinspires.ftc.teamcode.utilClass.varConfigurations.LiftVars
import org.firstinspires.ftc.teamcode.utilClass.varConfigurations.PIDVals
import org.gentrifiedApps.statemachineftc.SequentialRunSM

class DriverAid(
    private val scoringSubsystem: ScoringSubsystem,
    public val armSubsystem: ArmSubsystem,
    private val localizerSubsystem: LocalizerSubsystem,
    val auto: Boolean
) {
    init {
        daFunc = DAFunc(DAState.IDLE, {}, null, null, null, armSubsystem)
    }

    var usingDA = false

    enum class DAState {
        IDLE,
        COLLAPSE,
        HIGH_SPECIMEN,
        HIGH_BASKET,
        PICKUP,
        HUMAN,
        auto
    }

    fun auto() {
        autoScoreFunc.runInit()
    }

    fun collapse() {
        collapseFunc.runInit()
    }

    fun highSpecimen() {
        highSpecimenFunc.runInit()
    }

    fun highBasket() {
        highBasketFunc.runInit()
    }

    fun pickup() {
        pickupFunc.runInit()
    }

    fun human() {
        humanFunc.runInit()
    }


    companion object {

        private var collapseE = 0.0
        private var collapseP = 100.0
        private var hSpecimenE = 1200.0
        private var hSpecimenP = 1300.0
        private var hBasketE = 2250.0
        private var hBasketP = 2000.0
        private var pickupE = 1300.0
        var pickupP = 100.0
        private var humanE = 200.0
        private var humanP = 300.0
        var daState = DAState.IDLE
        lateinit var daFunc: DAFunc
    }

    private var pickupOnce = 0

    private val useConfig = false
    fun update() {
        if (useConfig) {
            collapseE = DAVars.collapseE
            collapseP = DAVars.collapseP
            hSpecimenE = DAVars.hSpecimenE
            hSpecimenP = DAVars.hSpecimenP
            hBasketE = DAVars.hBasketE
            hBasketP = DAVars.hBasketP
            pickupE = DAVars.pickUpE
            pickupP = DAVars.pickUpP
            humanE = DAVars.humanE
            humanP = DAVars.humanP
        }
        daFunc.runArmSub()
    }

    val autoScoreFunc = DAFunc(DAState.auto, {
        scoringSubsystem.closeClaw()
        scoringSubsystem.setPitchMed()
    }, Triple(2000.0, 0.0, null), null, null, armSubsystem)

    val collapseFunc = DAFunc(DAState.COLLAPSE, {
        scoringSubsystem.closeClaw()
    }, Triple(collapseP, collapseE, false), null, null, armSubsystem)

    val highSpecimenFunc = DAFunc(DAState.HIGH_SPECIMEN, {
        scoringSubsystem.specimenRotate(armSubsystem.pAngle())
        scoringSubsystem.setRotateCenter()
    }, null, { armSubsystem.setHeight(24.5, 30.0, true, true) }, null, armSubsystem)

    val highBasketFunc = DAFunc(DAState.HIGH_BASKET, {
        scoringSubsystem.setPitchMed()
        scoringSubsystem.setRotateCenter()
    }, Triple(hBasketP, hBasketE, true), null, {
        armSubsystem.pMax = 0.5
    }, armSubsystem)

    val pickupFunc = DAFunc(
        DAState.PICKUP,
        {
            scoringSubsystem.setPitchMed()
            scoringSubsystem.setRotateCenter()
            scoringSubsystem.openClaw()
        }, Triple(pickupP, pickupE, false), null, {
            PIDVals.pitchPIDFCo.d = 0.00025
        }, armSubsystem
    )

    val humanFunc = DAFunc(DAState.HUMAN, {
        scoringSubsystem.setPitchMed()
        scoringSubsystem.setRotateCenter()
        scoringSubsystem.openClaw()
    }, Triple(humanP - if (auto) 50 else 25, humanE, false), null, null, armSubsystem)

    fun isDone(tolerance: Double): Boolean {
        return daFunc.isEnded(tolerance)
    }

    class DAFunc(
        val state: DAState,
        private val funcs: Runnable,
        private val setPE: Triple<Double, Double, Boolean?>?,
        private val armSubFunc: Runnable?,
        private val armPowerFunc: Runnable?,
        private val armSubsystem: ArmSubsystem
    ) {
        fun runInit() {
            armSubsystem.resetHitPosition()
            daState = state
            funcs.run()
            daFunc = this
        }

        fun runArmSub() {
            armPowerFunc?.run()
            if (setPE != null) {
                armSubsystem.setPE(setPE.first, setPE.second, setPE.third)
            } else {
                armSubFunc?.run()
            }
        }

        fun isEnded(tolerance: Double): Boolean {
            return armSubsystem.bothAtTarget(tolerance)
        }
    }

    class DAActions(
        val funcs: List<Runnable>,
    ) : Action {
        override fun run(packet: TelemetryPacket): Boolean {
            funcs.forEach(Runnable::run)
            return false
        }
    }

    fun daAction(funcs: List<Runnable>): Action {
        return DAActions(funcs)
    }

    private var liftState = false
    fun easyLiftL1() {
        usingDA = true
        if (!liftState) {
            scoringSubsystem.setPitchLow()
            scoringSubsystem.closeClaw()
            armSubsystem.setPE(LiftVars.step1P, LiftVars.step2E)
        }
        if (armSubsystem.isExtendAtTarget(100.0) && armSubsystem.isPitchAtTarget(200.0)) {
            liftState = true
            armSubsystem.setPE(LiftVars.step2P, LiftVars.step2E)
        }
    }

    fun lift() {
        usingDA = true
        if (!firstLevelLift.isStarted) {
            firstLevelLift.start()
        } else {
            firstLevelLift.update()
        }
    }

//    fun getRobotAngle(): Double {
//        return localizerSubsystem.poseUpdater.imuData.pitch
//    }

    enum class AutoLift {
        extend_pivot,
        hook1st,
        lift1st,
        pivotBack,
        sit1st,
        extend2nd,
        hook2nd,
        lift2nd,
        pivot2nd,
        sit2nd,
        collapse,
        stop
    }


    val firstLevelLift: SequentialRunSM<AutoLift> =
        SequentialRunSM.Builder<AutoLift>()
            .state(AutoLift.extend_pivot)
            .onEnter(AutoLift.extend_pivot) {
                scoringSubsystem.setPitchLow()
                scoringSubsystem.closeClaw()
                armSubsystem.setPE(LiftVars.step1P, LiftVars.step1E, false)
            }
            .transition(AutoLift.extend_pivot) {
                armSubsystem.setPE(LiftVars.step1P, LiftVars.step1E, false)
                armSubsystem.isPitchAtTarget(150.0) && armSubsystem.isExtendAtTarget()
            }
            .state(AutoLift.hook1st)
            .onEnter(AutoLift.hook1st) {
                scoringSubsystem.setPitchIdle()
                scoringSubsystem.setRotateIdle()
                armSubsystem.setPE(LiftVars.step2P, LiftVars.step2E)
            }
            .transition(AutoLift.hook1st) {
                armSubsystem.setPE(LiftVars.step2P, LiftVars.step2E)
                armSubsystem.isPitchAtTarget() && armSubsystem.isExtendAtTarget()
            }
            .state(AutoLift.hook2nd)
            .onEnter(AutoLift.hook2nd) {
                armSubsystem.setPE(100.0, LiftVars.step2E, false)
            }
            .transition(AutoLift.hook2nd) {
                armSubsystem.setPE(100.0, LiftVars.step2E, false)
                armSubsystem.isPitchAtTarget() && armSubsystem.isExtendAtTarget()
            }
            .stopRunning(AutoLift.stop)
            .build()
    val liftSequence: SequentialRunSM<AutoLift> =
        SequentialRunSM.Builder<AutoLift>()
            .state(AutoLift.extend_pivot)
            .onEnter(AutoLift.extend_pivot) {
                scoringSubsystem.setPitchLow()
                scoringSubsystem.closeClaw()
                armSubsystem.setPE(LiftVars.step1P, LiftVars.step1E)
            }
            .transition(AutoLift.extend_pivot) {
                armSubsystem.isPitchAtTarget(150.0) && armSubsystem.isExtendAtTarget()
            }
            .state(AutoLift.hook1st)
            .onEnter(AutoLift.hook1st) {
                armSubsystem.setPE(LiftVars.step2P, LiftVars.step2E)
            }
            .transition(AutoLift.hook1st) {
                armSubsystem.isPitchAtTarget() && armSubsystem.isExtendAtTarget()
            }
            .state(AutoLift.hook2nd)
            .onEnter(AutoLift.hook2nd) {
                armSubsystem.setPE(100.0, LiftVars.step2E, false)
            }
            .transition(AutoLift.hook2nd) {
                armSubsystem.isPitchAtTarget() && armSubsystem.isExtendAtTarget()
            }
            .state(AutoLift.lift1st)
            .onEnter(AutoLift.lift1st) {
                armSubsystem.setPE(LiftVars.step3P, LiftVars.step3E)
            }
            .transition(AutoLift.lift1st) {
                armSubsystem.isPitchAtTarget() && armSubsystem.isExtendAtTarget()
            }
            .stopRunning(AutoLift.stop)
            .build()
}