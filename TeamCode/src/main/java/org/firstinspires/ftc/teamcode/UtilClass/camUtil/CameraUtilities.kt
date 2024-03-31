package org.firstinspires.ftc.teamcode.UtilClass.camUtil

import android.util.Size
import com.acmerobotics.dashboard.FtcDashboard
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamSource
import org.firstinspires.ftc.teamcode.opModes.camera.VPObjectDetect
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor

object CameraUtilities {
    fun startCameraStream(streamSource: CameraStreamSource) {
        FtcDashboard.getInstance()
            .startCameraStream(streamSource, 0.0)
    }

    fun stopCameraStream() {
        FtcDashboard.getInstance()
            .stopCameraStream()
    }


    private lateinit var visionPortal: VisionPortal
    lateinit var aprilTag: AprilTagProcessor
    private lateinit var objProcessor: VPObjectDetect

    fun initializeProcessor(
        processor: Processor? = Processor.APRIL_TAG,
        ahwMap: HardwareMap,
        camera: String,
        ftcDashboard: Boolean
    ): Boolean {
        when (processor) {
            Processor.APRIL_TAG -> {
                aprilTag =
                    AprilTagProcessor.Builder() // The following default settings are available to un-comment and edit as needed.
                        .setDrawAxes(false)
                        .setDrawCubeProjection(false)
                        .setDrawTagOutline(true)
                        .setTagFamily(AprilTagProcessor.TagFamily.TAG_36h11)
                        .setTagLibrary(AprilTagGameDatabase.getCenterStageTagLibrary())
                        .setOutputUnits(DistanceUnit.INCH, AngleUnit.DEGREES)
                        .setLensIntrinsics(972.571, 972.571, 667.598, 309.012)
                        .build()

                // Adjust Image Decimation to trade-off detection-range for detection-rate.
                // eg: Some typical detection data using a Logitech C920 WebCam
                // Decimation = 1 ..  Detect 2" Tag from 10 feet away at 10 Frames per second
                // Decimation = 2 ..  Detect 2" Tag from 6  feet away at 22 Frames per second
                // Decimation = 3 ..  Detect 2" Tag from 4  feet away at 30 Frames Per Second (default)
                // Decimation = 3 ..  Detect 5" Tag from 10 feet away at 30 Frames Per Second (default)
                // Note: Decimation can be changed on-the-fly to adapt during a match.
                aprilTag.setDecimation(3.0F)
            }

            Processor.OBJECT_DETECT -> {
                objProcessor = VPObjectDetect()
            }

            null -> {
                throw Exception("Processor cannot be null")
            }
        }
        setupVisionPortal(processor, ahwMap, camera)
        if (ftcDashboard) {
            startCameraStream(visionPortal)
        }
        return true
    }

    private fun setupVisionPortal(processor: Processor, ahwMap: HardwareMap, camera: String) {
        visionPortal = VisionPortal.Builder()
            .setCamera(ahwMap.get(WebcamName::class.java, camera))
            .setCameraResolution(Size(1280, 720))
            .addProcessors(
                when (processor) {
                    Processor.OBJECT_DETECT -> objProcessor
                    Processor.APRIL_TAG -> aprilTag
                }
            )
            .build()
    }
}