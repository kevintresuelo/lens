package com.kevintresuelo.lens.screens

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kevintresuelo.adnoto.RatePrompter
import com.kevintresuelo.lens.R
import com.kevintresuelo.lens.billing.viewmodels.BillingViewModel
import com.kevintresuelo.lens.databinding.FragmentTranspositionBinding

class TranspositionFragment : Fragment() {

    private lateinit var binding: FragmentTranspositionBinding
    private var selectedFormula: LensFormula? = null

    private lateinit var billingViewModel: BillingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        /**
         * Initializes data binding with the layout and view container.
         */
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_transposition, container, false)


        /**
         * Notifies the host activity that this fragment has options menu.
         */
        setHasOptionsMenu(true)

        /**
         * Asks the user to rate the app on Google Play
         */
        RatePrompter(context)

        /**
         * Checks for new purchases of the user
         */
        billingViewModel = ViewModelProvider(this).get(BillingViewModel::class.java)

        MobileAds.initialize(requireContext()) {}
        billingViewModel.proVersionLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null || !it.entitled) {
                showAds()
            }
        })

        binding.ftCbMinusCylinderEnabled.setOnCheckedChangeListener { buttonView, isChecked ->
            processMinusCylinderCheckedChange(isChecked)
        }

        binding.ftCbPlusCylinderEnabled.setOnCheckedChangeListener { buttonView, isChecked ->
            processPlusCylinderCheckedChange(isChecked)
        }

        binding.ftCbCrossCylinderEnabled.setOnCheckedChangeListener { buttonView, isChecked ->
            processCrossCylinderCheckedChange(isChecked)
        }

        binding.ftBtnSubmit.setOnClickListener {
            transpose()
        }

        /**
         * Returns the root element of this inflated layout for the fragment to
         * handle.
         */
        return binding.root
    }

    private fun showAds() {
        val adRequest = AdRequest.Builder().build()
        binding.ftAvBottomAds.visibility = View.VISIBLE
        binding.ftAvBottomAds.loadAd(adRequest)
    }

    override fun onResume() {
        super.onResume()
        billingViewModel.queryPurchases()
    }

    /**
     * Inflates the menu from R.menu.menu_checker resource
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_transposition, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Navigates to specific fragments depending on the menu clicked
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mt_i_about -> this.findNavController().navigate(R.id.action_transpositionFragment_to_aboutFragment)
            R.id.mt_i_settings -> this.findNavController().navigate(R.id.action_transpositionFragment_to_settingsFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun processMinusCylinderCheckedChange(isChecked: Boolean) {

        if (isChecked) {
            selectedFormula = LensFormula.MINUS_CYLINDER
            uncheckOthersExcept(LensFormula.MINUS_CYLINDER)
            clearOthersExcept(LensFormula.MINUS_CYLINDER)
        }

        binding.ftTietMinusCylinderSpherePower.isEnabled = isChecked
        binding.ftTietMinusCylinderCylinderPower.isEnabled = isChecked
        binding.ftTietMinusCylinderCylinderAxis.isEnabled = isChecked
    }

    private fun processPlusCylinderCheckedChange(isChecked: Boolean) {

        if (isChecked) {
            selectedFormula = LensFormula.PLUS_CYLINDER
            uncheckOthersExcept(LensFormula.PLUS_CYLINDER)
            clearOthersExcept(LensFormula.PLUS_CYLINDER)
        }

        binding.ftTietPlusCylinderSpherePower.isEnabled = isChecked
        binding.ftTietPlusCylinderCylinderPower.isEnabled = isChecked
        binding.ftTietPlusCylinderCylinderAxis.isEnabled = isChecked
    }

    private fun processCrossCylinderCheckedChange(isChecked: Boolean) {

        if (isChecked) {
            selectedFormula = LensFormula.CROSS_CYLINDER
            uncheckOthersExcept(LensFormula.CROSS_CYLINDER)
            clearOthersExcept(LensFormula.CROSS_CYLINDER)
        }

        binding.ftTietCrossCylinderCylinderPower1.isEnabled = isChecked
        binding.ftTietCrossCylinderCylinderAxis1.isEnabled = isChecked
        binding.ftTietCrossCylinderCylinderPower2.isEnabled = isChecked
        binding.ftTietCrossCylinderCylinderAxis2.isEnabled = isChecked
    }

    private fun uncheckOthersExcept(lensFormula: LensFormula?) {
        if (lensFormula != LensFormula.MINUS_CYLINDER) {
            binding.ftCbMinusCylinderEnabled.isChecked = false
        }
        if (lensFormula != LensFormula.PLUS_CYLINDER) {
            binding.ftCbPlusCylinderEnabled.isChecked = false
        }
        if (lensFormula != LensFormula.CROSS_CYLINDER) {
            binding.ftCbCrossCylinderEnabled.isChecked = false
        }
    }

    private fun clearOthersExcept(lensFormula: LensFormula?) {
        if (lensFormula != LensFormula.MINUS_CYLINDER) {
            binding.ftTietMinusCylinderSpherePower.setText("")
            binding.ftTietMinusCylinderCylinderPower.setText("")
            binding.ftTietMinusCylinderCylinderAxis.setText("")
        }
        if (lensFormula != LensFormula.PLUS_CYLINDER) {
            binding.ftTietPlusCylinderSpherePower.setText("")
            binding.ftTietPlusCylinderCylinderPower.setText("")
            binding.ftTietPlusCylinderCylinderAxis.setText("")
        }
        if (lensFormula != LensFormula.CROSS_CYLINDER) {
            binding.ftTietCrossCylinderCylinderPower1.setText("")
            binding.ftTietCrossCylinderCylinderAxis1.setText("")
            binding.ftTietCrossCylinderCylinderPower2.setText("")
            binding.ftTietCrossCylinderCylinderAxis2.setText("")
        }
    }

    private fun transpose() {
        if (!hasSelectedLensFormula()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.transposition_error_unchecked_title)
                .setMessage(R.string.transposition_error_unchecked_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        if (!areInputsComplete()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.transposition_error_incomplete_title)
                .setMessage(R.string.transposition_error_incomplete_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }

        val inputData = getInputData()

        if (!areInputsValid(inputData)) {
            return
        }

        val rawData = getRawData(inputData)

        processResults(rawData)
    }

    private fun hasSelectedLensFormula(): Boolean {
        return (binding.ftCbMinusCylinderEnabled.isChecked ||
                binding.ftCbPlusCylinderEnabled.isChecked ||
                binding.ftCbCrossCylinderEnabled.isChecked) &&
                selectedFormula != null
    }

    private fun areInputsComplete(): Boolean {
        return when {
            selectedFormula == LensFormula.MINUS_CYLINDER &&
                    (binding.ftTietMinusCylinderSpherePower.text.isNullOrBlank() ||
                        binding.ftTietMinusCylinderCylinderPower.text.isNullOrBlank() ||
                        binding.ftTietMinusCylinderCylinderAxis.text.isNullOrBlank()) -> false
            selectedFormula == LensFormula.PLUS_CYLINDER &&
                    (binding.ftTietPlusCylinderSpherePower.text.isNullOrBlank() ||
                            binding.ftTietPlusCylinderCylinderPower.text.isNullOrBlank() ||
                            binding.ftTietPlusCylinderCylinderAxis.text.isNullOrBlank()) -> false
            selectedFormula == LensFormula.CROSS_CYLINDER &&
                    (binding.ftTietCrossCylinderCylinderPower1.text.isNullOrBlank() ||
                            binding.ftTietCrossCylinderCylinderAxis1.text.isNullOrBlank() ||
                            binding.ftTietCrossCylinderCylinderPower2.text.isNullOrBlank() ||
                            binding.ftTietCrossCylinderCylinderAxis2.text.isNullOrBlank()) -> false
            else -> true
        }
    }

    private fun getInputData(): LensPrescription {
        val firstPower = when (selectedFormula!!) {
            LensFormula.MINUS_CYLINDER -> binding.ftTietMinusCylinderSpherePower.text
            LensFormula.PLUS_CYLINDER -> binding.ftTietPlusCylinderSpherePower.text
            LensFormula.CROSS_CYLINDER -> binding.ftTietCrossCylinderCylinderPower1.text
        }.toString().toDouble()
        val firstAxis = when (selectedFormula!!) {
            LensFormula.CROSS_CYLINDER -> binding.ftTietCrossCylinderCylinderAxis1.text
            else -> "0"
        }.toString().toInt()
        val secondPower = when (selectedFormula!!) {
            LensFormula.MINUS_CYLINDER -> binding.ftTietMinusCylinderCylinderPower.text
            LensFormula.PLUS_CYLINDER -> binding.ftTietPlusCylinderCylinderPower.text
            LensFormula.CROSS_CYLINDER -> binding.ftTietCrossCylinderCylinderPower2.text
        }.toString().toDouble()
        val secondAxis = when (selectedFormula!!) {
            LensFormula.MINUS_CYLINDER -> binding.ftTietMinusCylinderCylinderAxis.text
            LensFormula.PLUS_CYLINDER -> binding.ftTietPlusCylinderCylinderAxis.text
            LensFormula.CROSS_CYLINDER -> binding.ftTietCrossCylinderCylinderAxis2.text
        }.toString().toInt()

        return LensPrescription(firstPower, firstAxis, secondPower, secondAxis)
    }

    private fun getRawData(inputData: LensPrescription): LensPrescription {
        val firstMeridian = when (selectedFormula!!) {
            LensFormula.MINUS_CYLINDER -> inputData.secondAxis
            LensFormula.PLUS_CYLINDER -> inputData.secondAxis
            LensFormula.CROSS_CYLINDER -> inputData.secondAxis
        }
        val firstMeridianPower = when (selectedFormula!!) {
            LensFormula.MINUS_CYLINDER -> inputData.firstPower
            LensFormula.PLUS_CYLINDER -> inputData.firstPower
            LensFormula.CROSS_CYLINDER -> inputData.firstPower
        }
        val secondMeridian = when (selectedFormula!!) {
            LensFormula.MINUS_CYLINDER -> getOppositeAxis(inputData.secondAxis)
            LensFormula.PLUS_CYLINDER -> getOppositeAxis(inputData.secondAxis)
            LensFormula.CROSS_CYLINDER -> inputData.firstAxis
        }
        val secondMeridianPower = when (selectedFormula!!) {
            LensFormula.MINUS_CYLINDER -> inputData.firstPower + inputData.secondPower
            LensFormula.PLUS_CYLINDER -> inputData.firstPower + inputData.secondPower
            LensFormula.CROSS_CYLINDER -> inputData.secondPower
        }

        return LensPrescription(firstMeridianPower, firstMeridian, secondMeridianPower, secondMeridian)
    }

    private fun areInputsValid(inputData: LensPrescription): Boolean {
        if (selectedFormula == LensFormula.MINUS_CYLINDER && inputData.secondPower > 0) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.transposition_error_minus_cylinder_plus_power_title)
                .setMessage(R.string.transposition_error_minus_cylinder_plus_power_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return false
        } else if (selectedFormula == LensFormula.PLUS_CYLINDER && inputData.secondPower < 0) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.transposition_error_plus_cylinder_minus_power_title)
                .setMessage(R.string.transposition_error_plus_cylinder_minus_power_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return false
        } else if (selectedFormula == LensFormula.CROSS_CYLINDER && inputData.firstAxis != getOppositeAxis(inputData.secondAxis)) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.transposition_error_cross_cylinder_wrong_axis_title)
                .setMessage(R.string.transposition_error_cross_cylinder_wrong_axis_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return false
        }
        return checkIfValidAxes(inputData)
    }

    private fun checkIfValidAxes(inputData: LensPrescription): Boolean {
        return if (inputData.firstAxis in 0..180 && inputData.secondAxis in 0..180) {
            true
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.transposition_error_axis_out_of_bounds_title)
                .setMessage(R.string.transposition_error_axis_out_of_bounds_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            false
        }
    }

    private fun getOppositeAxis(axis: Int): Int {
        return if (axis > 90) {
            axis - 90
        } else {
            axis + 90
        }
    }

    private fun processResults(rawData: LensPrescription) {
        val rawDataHigherPower: Double
        val rawDataLowerPower: Double
        val rawDataHigherPowerAxis: Int
        val rawDataLowerPowerAxis: Int

        if (rawData.firstPower > rawData.secondPower) {
            rawDataHigherPower = rawData.firstPower
            rawDataLowerPower = rawData.secondPower
            rawDataHigherPowerAxis = rawData.firstAxis
            rawDataLowerPowerAxis = rawData.secondAxis
        } else {
            rawDataHigherPower = rawData.secondPower
            rawDataLowerPower = rawData.firstPower
            rawDataHigherPowerAxis = rawData.secondAxis
            rawDataLowerPowerAxis = rawData.firstAxis
        }

        val minusCylinderSpherePower = rawDataHigherPower
        val minusCylinderSphereAxis = getOppositeAxis(rawDataHigherPowerAxis)
        val minusCylinderCylinderPower = rawDataLowerPower - rawDataHigherPower
        val minusCylinderCylinderAxis = rawDataHigherPowerAxis
        val minusCylinder = LensPrescription(minusCylinderSpherePower, minusCylinderSphereAxis, minusCylinderCylinderPower, minusCylinderCylinderAxis)
        showMinusCylinderResult(minusCylinder)

        val plusCylinderSpherePower = rawDataLowerPower
        val plusCylinderSphereAxis = getOppositeAxis(rawDataLowerPowerAxis)
        val plusCylinderCylinderPower = rawDataHigherPower - rawDataLowerPower
        val plusCylinderCylinderAxis = rawDataLowerPowerAxis
        val plusCylinder = LensPrescription(plusCylinderSpherePower, plusCylinderSphereAxis, plusCylinderCylinderPower, plusCylinderCylinderAxis)
        showPlusCylinderResult(plusCylinder)

        val crossCylinderCylinderPower1 = rawData.firstPower
        val crossCylinderCylinderAxis1 = rawData.secondAxis
        val crossCylinderCylinderPower2 = rawData.secondPower
        val crossCylinderCylinderAxis2 = rawData.firstAxis
        val crossCylinder = LensPrescription(crossCylinderCylinderPower1, crossCylinderCylinderAxis1, crossCylinderCylinderPower2, crossCylinderCylinderAxis2)
        showCrossCylinderResult(crossCylinder)

    }

    private fun showMinusCylinderResult(minusCylinder: LensPrescription) {
        binding.ftTietMinusCylinderSpherePower.setText(String.format("%.2f",minusCylinder.firstPower))
        binding.ftTietMinusCylinderCylinderPower.setText(String.format("%.2f",minusCylinder.secondPower))
        binding.ftTietMinusCylinderCylinderAxis.setText(minusCylinder.secondAxis.toString())
    }

    private fun showPlusCylinderResult(plusCylinder: LensPrescription) {
        binding.ftTietPlusCylinderSpherePower.setText(String.format("%.2f",plusCylinder.firstPower))
        binding.ftTietPlusCylinderCylinderPower.setText(String.format("%.2f",plusCylinder.secondPower))
        binding.ftTietPlusCylinderCylinderAxis.setText(plusCylinder.secondAxis.toString())
    }

    private fun showCrossCylinderResult(crossCylinder: LensPrescription) {
        binding.ftTietCrossCylinderCylinderPower1.setText(String.format("%.2f",crossCylinder.firstPower))
        binding.ftTietCrossCylinderCylinderAxis1.setText(crossCylinder.firstAxis.toString())
        binding.ftTietCrossCylinderCylinderPower2.setText(String.format("%.2f",crossCylinder.secondPower))
        binding.ftTietCrossCylinderCylinderAxis2.setText(crossCylinder.secondAxis.toString())
    }

    private enum class LensFormula {
        MINUS_CYLINDER,
        PLUS_CYLINDER,
        CROSS_CYLINDER
    }

    private data class LensPrescription (
        val firstPower: Double,
        val firstAxis: Int,
        val secondPower: Double,
        val secondAxis: Int
    )
}
