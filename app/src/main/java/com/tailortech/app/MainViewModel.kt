package com.tailortech.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.tailortech.app.data.MeasurementsRepository
import com.tailortech.app.data.UnitSystem
import com.tailortech.app.data.UserMeasurements
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TailorTab {
    DASHBOARD,
    STUDIO,
    SIZE_INSIGHTS
}

class MainViewModel(
    private val repository: MeasurementsRepository
) : ViewModel() {
    val measurements = repository.observeMeasurements().stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = UserMeasurements()
    )

    private val _selectedTab = MutableStateFlow(TailorTab.DASHBOARD)
    val selectedTab: StateFlow<TailorTab> = _selectedTab.asStateFlow()

    private val _selectedCountry = MutableStateFlow("GB")
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()

    fun selectTab(tab: TailorTab) {
        _selectedTab.value = tab
    }

    fun selectCountry(countryCode: String) {
        _selectedCountry.value = countryCode
    }

    fun updateField(field: MeasurementField, value: Double) {
        val current = measurements.value
        val updated = when (field) {
            MeasurementField.HEAD_FRONT_TO_BACK -> current.copy(headFrontToBackCm = value)
            MeasurementField.NECK -> current.copy(neckCm = value)
            MeasurementField.CHEST_UPPER -> current.copy(chestUpperCm = value)
            MeasurementField.CHEST_LOWER -> current.copy(chestLowerCm = value)
            MeasurementField.WAIST_NATURAL -> current.copy(waistNaturalCm = value)
            MeasurementField.WAIST_PANTS -> current.copy(waistPantsLevelCm = value)
            MeasurementField.HIP -> current.copy(hipCm = value)
            MeasurementField.SHOULDER_TO_SHOULDER -> current.copy(shoulderToShoulderCm = value)
            MeasurementField.NECK_TO_SHOULDER -> current.copy(neckToShoulderCm = value)
            MeasurementField.NECK_TO_WAIST -> current.copy(neckToWaistCm = value)
            MeasurementField.ARMSCYE -> current.copy(armscyeCm = value)
            MeasurementField.BICEP_FLEXED -> current.copy(bicepFlexedCm = value)
            MeasurementField.BICEP_RELAXED -> current.copy(bicepRelaxedCm = value)
            MeasurementField.WRIST -> current.copy(wristCm = value)
            MeasurementField.WRIST_TO_MIDDLE_FINGER -> current.copy(wristToMiddleFingerCm = value)
            MeasurementField.INNER_THIGH_TO_KNEE -> current.copy(innerThighToKneeCm = value)
            MeasurementField.KNEE_TO_ANKLE -> current.copy(kneeToAnkleCm = value)
            MeasurementField.KNEE -> current.copy(kneeCm = value)
            MeasurementField.CALF_WIDEST -> current.copy(calfWidestCm = value)
            MeasurementField.ANKLE -> current.copy(ankleCm = value)
            MeasurementField.OUTER_THIGH -> current.copy(outerThighCm = value)
            MeasurementField.FOOT_LENGTH -> current.copy(footLengthCm = value)
            MeasurementField.FOOT_WIDTH -> current.copy(footWidthCm = value)
            MeasurementField.SHOULDER_TO_ELBOW -> current.copy(shoulderToElbowCm = value)
            MeasurementField.ELBOW_TO_WRIST -> current.copy(elbowToWristCm = value)
            MeasurementField.RISE -> current.copy(riseCrotchCm = value)
            MeasurementField.THIGH_WIDEST -> current.copy(thighWidestCm = value)
            MeasurementField.HEIGHT -> current.copy(heightCm = value)
        }
        viewModelScope.launch {
            repository.save(updated)
        }
    }

    fun toggleUnitSystem() {
        val current = measurements.value
        val next = if (current.unitSystem == UnitSystem.CM) UnitSystem.INCH else UnitSystem.CM
        viewModelScope.launch {
            repository.save(current.copy(unitSystem = next))
        }
    }
}

class MainViewModelFactory(
    private val repository: MeasurementsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository) as T
    }
}

enum class MeasurementField(val label: String) {
    HEIGHT("Height"),
    HEAD_FRONT_TO_BACK("Head Front-to-Back"),
    NECK("Neck"),
    CHEST_UPPER("Chest Upper"),
    CHEST_LOWER("Chest Lower"),
    WAIST_NATURAL("Waist Natural"),
    WAIST_PANTS("Waist Pants Level"),
    HIP("Hip"),
    SHOULDER_TO_SHOULDER("Shoulder to Shoulder"),
    NECK_TO_SHOULDER("Neck to Shoulder"),
    NECK_TO_WAIST("Neck to Waist"),
    ARMSCYE("Armscye"),
    BICEP_FLEXED("Bicep Flexed"),
    BICEP_RELAXED("Bicep Relaxed"),
    WRIST("Wrist"),
    WRIST_TO_MIDDLE_FINGER("Wrist to Middle Finger"),
    INNER_THIGH_TO_KNEE("Inner Thigh to Knee"),
    KNEE_TO_ANKLE("Knee to Ankle"),
    KNEE("Knee"),
    CALF_WIDEST("Calf Widest"),
    ANKLE("Ankle"),
    OUTER_THIGH("Outer Thigh"),
    FOOT_LENGTH("Foot Length"),
    FOOT_WIDTH("Foot Width"),
    SHOULDER_TO_ELBOW("Shoulder to Elbow"),
    ELBOW_TO_WRIST("Elbow to Wrist"),
    RISE("Rise"),
    THIGH_WIDEST("Thigh Widest")
}
