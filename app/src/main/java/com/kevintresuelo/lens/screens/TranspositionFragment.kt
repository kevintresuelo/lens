package com.kevintresuelo.lens.screens

import android.graphics.Color
import android.opengl.Matrix
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
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
import java.lang.NumberFormatException

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
         * Asks the user to rate the app on Google Play.
         */
        RatePrompter(context)

        /**
         * Checks for new purchases of the user.
         */
        billingViewModel = ViewModelProvider(this).get(BillingViewModel::class.java)

        /**
         * Loads ads if the user isn't entitled to Pro version.
         */
        MobileAds.initialize(requireContext()) {}
        billingViewModel.proVersionLiveData.observe(viewLifecycleOwner, Observer {
            if (it == null || !it.entitled) {
                showAds()
            } else {
                (activity as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name_pro)
            }
        })

        /**
         * Alters the UI based on whether the minus cylinder form is checked.
         */
        binding.ftCbMinusCylinderEnabled.setOnCheckedChangeListener { buttonView, isChecked ->
            processMinusCylinderCheckedChange(isChecked)
        }

        /**
         * Alters the UI based on whether the plus cylinder form is checked.
         */
        binding.ftCbPlusCylinderEnabled.setOnCheckedChangeListener { buttonView, isChecked ->
            processPlusCylinderCheckedChange(isChecked)
        }

        /**
         * Alters the UI based on whether the cross cylinder form is checked.
         */
        binding.ftCbCrossCylinderEnabled.setOnCheckedChangeListener { buttonView, isChecked ->
            processCrossCylinderCheckedChange(isChecked)
        }

        /**
         * Starts the transposition process.
         */
        binding.ftBtnSubmit.setOnClickListener {
            transpose()
        }

        /**
         * Returns the root element of this inflated layout for the fragment to
         * handle.
         */
        return binding.root
    }

    /**
     * Shows ads by loading the ad request to the AdView.
     */
    private fun showAds() {
        Log.d("TAG","showAds")
        val adRequest = AdRequest.Builder().build()
        binding.ftAvBottomAds.visibility = View.VISIBLE
        binding.ftAvBottomAds.loadAd(adRequest)
    }

    override fun onResume() {
        super.onResume()
        billingViewModel.queryPurchases()
    }

    /**
     * Inflates the menu from R.menu.menu_checker resource.
     */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_transposition, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * Navigates to specific fragments depending on the menu clicked.
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mt_i_about -> this.findNavController().navigate(R.id.action_transpositionFragment_to_aboutFragment)
            R.id.mt_i_settings -> this.findNavController().navigate(R.id.action_transpositionFragment_to_settingsFragment)
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Updates the UI based on whether the minus cylinder form is checked.
     */
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

    /**
     * Updates the UI based on whether the plus cylinder form is checked.
     */
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

    /**
     * Updates the UI based on whether the cross cylinder form is checked.
     */
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

    /**
     * Unchecks other lens forms except for the one provided.
     */
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

    /**
     * Clears other lens forms except for the one provided.
     */
    private fun clearOthersExcept(lensFormula: LensFormula?) {
        if (lensFormula != LensFormula.MINUS_CYLINDER) {
            binding.ftTietMinusCylinderSpherePower.setText("")
            binding.ftTietMinusCylinderCylinderPower.setText("")
            binding.ftTietMinusCylinderCylinderAxis.setText("")
            binding.ftTvMinusCylinderSummary.setText(R.string.transposition_minus_cylinder_summary)
        }
        if (lensFormula != LensFormula.PLUS_CYLINDER) {
            binding.ftTietPlusCylinderSpherePower.setText("")
            binding.ftTietPlusCylinderCylinderPower.setText("")
            binding.ftTietPlusCylinderCylinderAxis.setText("")
            binding.ftTvPlusCylinderSummary.setText(R.string.transposition_plus_cylinder_summary)
        }
        if (lensFormula != LensFormula.CROSS_CYLINDER) {
            binding.ftTietCrossCylinderCylinderPower1.setText("")
            binding.ftTietCrossCylinderCylinderAxis1.setText("")
            binding.ftTietCrossCylinderCylinderPower2.setText("")
            binding.ftTietCrossCylinderCylinderAxis2.setText("")
            binding.ftTvCrossCylinderSummary.setText(R.string.transposition_cross_cylinder_summary)
        }
        binding.ftIvOpticalCrossAxis.visibility = View.GONE
        binding.ftTvOpticalCrossSummary.setText(R.string.transposition_optical_cross_summary)
    }

    /**
     * Transposes the given data to other lens forms.
     */
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

        val inputData = getInputData() ?: return

        if (!areInputsValid(inputData)) {
            return
        }

        val rawData = getRawData(inputData)

        processResults(rawData)
    }

    /**
     * Checks whether at least one lens formula is selected.
     */
    private fun hasSelectedLensFormula(): Boolean {
        return (binding.ftCbMinusCylinderEnabled.isChecked ||
                binding.ftCbPlusCylinderEnabled.isChecked ||
                binding.ftCbCrossCylinderEnabled.isChecked) &&
                selectedFormula != null
    }

    /**
     * Checks all input data for completeness.
     */
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

    /**
     * Parses the input data into a Lens Prescription type.
     */
    private fun getInputData(): LensPrescription? {
        try {
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
        } catch (e: NumberFormatException) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.transposition_error_unparsable_title)
                .setMessage(R.string.transposition_error_unparsable_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return null
        }
    }

    /**
     * Returns the raw data as seen on an optical cross.
     */
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

    /**
     * Checks all input data for validity.
     */
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

    /**
     * Checks the given axes if they are indeed 90 degrees apart.
     */
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

    /**
     * Returns the opposite axis of the given angle.
     */
    private fun getOppositeAxis(axis: Int): Int {
        return if (axis > 90) {
            axis - 90
        } else {
            axis + 90
        }
    }

    /**
     * Parses the result into the different lens forms and passes it onto the
     * appropriate UI handler.
     */
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

        showOpticalCross(rawData)
    }

    /**
     * Updates the UI to reflect the minus cylinder form result.
     */
    private fun showMinusCylinderResult(minusCylinder: LensPrescription) {
        val firstPower = String.format("%.2f",minusCylinder.firstPower)
        val secondPower = String.format("%.2f", minusCylinder.secondPower)
        val secondAxis = minusCylinder.secondAxis.toString()
        val parsedResult = "$firstPower ${getString(R.string.transposition_unit_sphere_power)} $secondPower ${getString(R.string.transposition_unit_cylinder_power)} ${getString(R.string.transposition_unit_axis)} $secondAxis"

        binding.ftTietMinusCylinderSpherePower.setText(firstPower)
        binding.ftTietMinusCylinderCylinderPower.setText(secondPower)
        binding.ftTietMinusCylinderCylinderAxis.setText(secondAxis)
        binding.ftTvMinusCylinderSummary.text = parsedResult
    }

    /**
     * Updates the UI to reflect the plus cylinder form result.
     */

    private fun showPlusCylinderResult(plusCylinder: LensPrescription) {
        val firstPower = String.format("%.2f", plusCylinder.firstPower)
        val secondPower = String.format("%.2f", plusCylinder.secondPower)
        val secondAxis = plusCylinder.secondAxis.toString()
        val parsedResult = "$firstPower ${getString(R.string.transposition_unit_sphere_power)} $secondPower ${getString(R.string.transposition_unit_cylinder_power)} ${getString(R.string.transposition_unit_axis)} $secondAxis"

        binding.ftTietPlusCylinderSpherePower.setText(firstPower)
        binding.ftTietPlusCylinderCylinderPower.setText(secondPower)
        binding.ftTietPlusCylinderCylinderAxis.setText(secondAxis)
        binding.ftTvPlusCylinderSummary.text = parsedResult
    }

    /**
     * Updates the UI to reflect the cross cylinder form result.
     */
    private fun showCrossCylinderResult(crossCylinder: LensPrescription) {
        val firstPower = String.format("%.2f", crossCylinder.firstPower)
        val firstAxis = crossCylinder.firstAxis.toString()
        val secondPower = String.format("%.2f", crossCylinder.secondPower)
        val secondAxis = crossCylinder.secondAxis.toString()
        val parsedResult = "$firstPower ${getString(R.string.transposition_unit_cylinder_power)} ${getString(R.string.transposition_unit_axis)} $firstAxis   $secondPower ${getString(R.string.transposition_unit_cylinder_power)} ${getString(R.string.transposition_unit_axis)} $secondAxis\n\n$secondPower ${getString(R.string.transposition_unit_cylinder_power)} ${getString(R.string.transposition_unit_axis)} $secondAxis   $firstPower ${getString(R.string.transposition_unit_cylinder_power)} ${getString(R.string.transposition_unit_axis)} $firstAxis"

        binding.ftTietCrossCylinderCylinderPower1.setText(firstPower)
        binding.ftTietCrossCylinderCylinderAxis1.setText(firstAxis)
        binding.ftTietCrossCylinderCylinderPower2.setText(secondPower)
        binding.ftTietCrossCylinderCylinderAxis2.setText(secondAxis)
        binding.ftTvCrossCylinderSummary.text = parsedResult
    }

    /**
     * Updates the UI to reflect the optical cross result.
     */
    private fun showOpticalCross(rawData: LensPrescription) {
        val firstMeridian = "M${rawData.firstAxis}: ${String.format("%.2f", rawData.firstPower)} D"
        val secondMeridian = "M${rawData.secondAxis}: ${String.format("%.2f", rawData.secondPower)} D"

        binding.ftIvOpticalCrossAxis.visibility = View.VISIBLE
        binding.ftIvOpticalCrossAxis.rotation = -rawData.firstAxis.toFloat()
        binding.ftTvOpticalCrossSummary.text = if (Build.VERSION.SDK_INT >= 24) {
            Html.fromHtml("<font color='#3dc17a'>$firstMeridian</font><br /> <br /><font color='#c13d83'>$secondMeridian</font>", Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml("<font color='#3dc17a'>$firstMeridian</font><br /> <br /><font color='#c13d83'>$secondMeridian</font>")
        }
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
