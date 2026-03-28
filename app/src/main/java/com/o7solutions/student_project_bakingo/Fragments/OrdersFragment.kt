package com.o7solutions.student_project_bakingo.Fragments

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.google.firebase.database.*
import com.o7solutions.student_project_bakingo.Adapters.OrdersAdapter
import com.o7solutions.student_project_bakingo.databinding.FragmentOrdersBinding
import java.text.SimpleDateFormat
import java.util.*

class OrdersFragment : Fragment() {

    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    private lateinit var databaseRef: DatabaseReference
    private val allOrdersList = mutableListOf<Order>()

    // Track the selected Month and Year using a Calendar object
    private var selectedCalendar = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databaseRef = FirebaseDatabase.getInstance().getReference("Orders")

        // Initial setup for current month
        updateDateButtonText()

        binding.btnPickDate.setOnClickListener { showMonthYearPicker() }

        setupRecyclerView()
        fetchDataFromFirebase()
    }

    private fun updateDateButtonText() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.btnPickDate.text = "Selected Month: ${sdf.format(selectedCalendar.time)}"
    }

    private fun showMonthYearPicker() {
        val c = Calendar.getInstance()
        // Standard DatePickerDialog: We pick a date, but we only store Month and Year
        val datePicker = DatePickerDialog(requireContext(), { _, year, month, _ ->
            selectedCalendar.set(Calendar.YEAR, year)
            selectedCalendar.set(Calendar.MONTH, month)
            updateDateButtonText()
            updateUIForSelectedMonth()
        }, selectedCalendar.get(Calendar.YEAR), selectedCalendar.get(Calendar.MONTH), 1)

        datePicker.setTitle("Select Month")
        datePicker.show()
    }

    private fun setupRecyclerView() {
        binding.rvOrders.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = true
            stackFromEnd = true
        }
    }

    private fun fetchDataFromFirebase() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allOrdersList.clear()
                for (data in snapshot.children) {
                    val order = data.getValue(Order::class.java)
                    order?.let { allOrdersList.add(it.copy(orderId = data.key ?: "")) }
                }
                updateUIForSelectedMonth()
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateUIForSelectedMonth() {
        val selectedMonth = selectedCalendar.get(Calendar.MONTH)
        val selectedYear = selectedCalendar.get(Calendar.YEAR)

        // Filter orders for the chosen Month and Year
        val filteredOrders = allOrdersList.filter {
            val orderCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            orderCal.get(Calendar.MONTH) == selectedMonth &&
                    orderCal.get(Calendar.YEAR) == selectedYear
        }

        // Update RecyclerView
        binding.rvOrders.adapter = OrdersAdapter(filteredOrders) { id ->
            databaseRef.child(id).child("paymentStatus").setValue("Completed")
        }

        // Calculate Monthly Insights and Graph
        calculateMonthlyInsights(filteredOrders)

        binding.tvNoOrders.visibility = if (filteredOrders.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun calculateMonthlyInsights(list: List<Order>) {
        var totalMonthlyProfit = 0.0
        val ordersPerDay = mutableMapOf<Int, Int>()

        // Initialize every day of the month with 0 so the line graph shows the whole month
        val maxDaysInMonth = selectedCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..maxDaysInMonth) {
            ordersPerDay[i] = 0
        }

        for (order in list) {
            // Price calculation
            val price = order.itemPrice.replace(Regex("[^0-9.]"), "").toDoubleOrNull() ?: 0.0
            totalMonthlyProfit += price

            // Grouping by Day
            val orderCal = Calendar.getInstance().apply { timeInMillis = order.timestamp }
            val dayOfMonth = orderCal.get(Calendar.DAY_OF_MONTH)
            ordersPerDay[dayOfMonth] = ordersPerDay.getOrDefault(dayOfMonth, 0) + 1
        }

        binding.tvTotalProfit.text = "Monthly Profit: ₹${String.format("%.2f", totalMonthlyProfit)}"

        // Find top seller within this month
        val productCounts = list.groupingBy { it.itemName }.eachCount()
        val maxSold = productCounts.maxByOrNull { it.value }
        binding.tvBestSeller.text = if (maxSold != null) {
            "Top Product: ${maxSold.key} (${maxSold.value} units)"
        } else {
            "Top Product: N/A"
        }

        updateLineChart(ordersPerDay)
    }

    private fun updateLineChart(dayCounts: Map<Int, Int>) {
        val entries = mutableListOf<Entry>()

        // Convert map to entries (X = Day of Month, Y = Number of Orders)
        dayCounts.keys.sorted().forEach { day ->
            entries.add(Entry(day.toFloat(), dayCounts[day]?.toFloat() ?: 0f))
        }

        val dataSet = LineDataSet(entries, "Orders per Day").apply {
            color = Color.parseColor("#BB86FC")
            setCircleColor(Color.parseColor("#03DAC5"))
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(true)
            valueTextSize = 8f
            mode = LineDataSet.Mode.HORIZONTAL_BEZIER // Smooth curves
            setDrawFilled(true) // Fills area under line
            fillColor = Color.parseColor("#BB86FC")
            fillAlpha = 40
        }

        binding.lineChart.apply { // Note: ID in XML should be lineChart
            data = LineData(dataSet)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
                labelCount = 10
            }

            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f // Start Y axis at 0
            }

            axisRight.isEnabled = false // Hide right axis for cleaner look
            description.isEnabled = false
            animateX(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}