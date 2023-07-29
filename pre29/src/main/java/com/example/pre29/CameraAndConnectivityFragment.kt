package com.example.pre29

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.pre29.databinding.FragmentCameraAndConnectivityBinding

class CameraAndConnectivityFragment :Fragment() {

    private lateinit var binding: FragmentCameraAndConnectivityBinding

    private val cameraSettingsRecyclerAdapter = CameraSettingsRecyclerAdapter()

    private val characteristicsCodesList = buildCodesList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraAndConnectivityBinding.inflate(inflater, container, false)
        binding.cameraSettingsRecycler.adapter = cameraSettingsRecyclerAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val characteristics = cameraManager.getCameraCharacteristics(cameraManager.cameraIdList.first())
        val items = characteristicsCodesList.map {
            parseItem(characteristics, it)
        }

        cameraSettingsRecyclerAdapter.items = items
    }

    private fun buildCodesList(): List<CameraCharacteristics.Key<out Any>> {
        val list = ArrayList<CameraCharacteristics.Key<out Any>>()
        list.add(CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE)
        list.add(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)
        list.add(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            list.add(CameraCharacteristics.INFO_VERSION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            list.add(CameraCharacteristics.LENS_DISTORTION)

        list.add(CameraCharacteristics.LENS_FACING)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            list.add(CameraCharacteristics.LENS_POSE_REFERENCE)

        list.add(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
        list.add(CameraCharacteristics.SENSOR_INFO_LENS_SHADING_APPLIED)
        list.add(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)

        return list
    }

    private fun parseItem(
        characteristics: CameraCharacteristics,
        key: CameraCharacteristics.Key<out Any>
    ): CameraSetting {

        val settingValue = when (key) {
            CameraCharacteristics.CONTROL_AWB_LOCK_AVAILABLE ->
                characteristics.get(key).toString()

            CameraCharacteristics.CONTROL_MAX_REGIONS_AE ->
                characteristics.get(key).toString()

            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL ->
                when (characteristics.get(key) as Int) {
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY -> "Legacy"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED -> "Limited"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL -> "Full"
                    CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3 -> "Level3"
                    else -> "External"
                }

            CameraCharacteristics.LENS_FACING ->
                when (characteristics.get(key) as Int) {
                    CameraMetadata.LENS_FACING_FRONT -> "Front"
                    CameraMetadata.LENS_FACING_BACK -> "Back"
                    else -> "External"
                }

            CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM ->
                characteristics.get(key).toString()

            CameraCharacteristics.SENSOR_INFO_LENS_SHADING_APPLIED ->
                characteristics.get(key)?.toString() ?: "Not available"

            CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE ->
                (characteristics.get(key) as Size)
                    .let { "${it.height} x ${it.width}" }

            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                when (key) {
                    CameraCharacteristics.INFO_VERSION ->
                        characteristics.get(key)?.toString() ?: "Not available"

                    CameraCharacteristics.LENS_DISTORTION ->
                        (characteristics.get(key) as? Array<*>)
                            ?.joinToString(", ")
                            ?: "Not available"

                    CameraCharacteristics.LENS_POSE_REFERENCE ->
                        when (characteristics.get(key) as? Int) {
                            CameraMetadata.LENS_POSE_REFERENCE_PRIMARY_CAMERA -> "Primary"
                            null -> "Not available"
                            else -> "Gyroscope"
                        }

                    else -> "Error"
                }
            } else "Error"
        }

        return CameraSetting(key.name, settingValue)
    }
}