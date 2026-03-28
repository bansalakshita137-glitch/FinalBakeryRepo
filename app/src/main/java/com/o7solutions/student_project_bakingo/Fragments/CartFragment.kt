package com.o7solutions.student_project_bakingo.Fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import com.o7solutions.student_project_bakingo.R
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.o7solutions.student_project_bakingo.Adapters.CartAdapter
import com.o7solutions.student_project_bakingo.*
import com.o7solutions.student_project_bakingo.databinding.FragmentCartBinding
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class CartFragment : Fragment(), PaymentResultListener {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private val cartList = mutableListOf<CartItemWrapper>()
    private val SMS_PERMISSION_CODE = 100

    // Temporary variables to hold data during Razorpay transition
    private var tempItem: CartData? = null
    private var tempCartKey: String = ""
    private var tempAddress: String = ""
    private var tempPhone: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        Checkout.preload(requireContext()) // Preload Razorpay
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        databaseRef = FirebaseDatabase.getInstance().getReference("Cart").child(userId)

        setupRecyclerView()
        fetchCartData()
    }

    private fun setupRecyclerView() {
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun fetchCartData() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (_binding == null || !isAdded) return
                cartList.clear()
                for (data in snapshot.children) {
                    val cartData = data.getValue(CartData::class.java)
                    val key = data.key
                    if (cartData != null && key != null) {
                        cartList.add(CartItemWrapper(key, cartData))
                    }
                }

                binding.rvCart.adapter = CartAdapter(cartList,
                    onDeleteClick = { key -> deleteItem(key) },
                    onOrderClick = { item, key -> showOrderDetailsDialog(item, key) }
                )

                binding.tvEmptyCart.visibility = if (cartList.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun showOrderDetailsDialog(item: CartData, cartKey: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(com.o7solutions.student_project_bakingo.R.layout.dialog_order_details, null)
        val etAddress = dialogView.findViewById<EditText>(R.id.etDeliveryAddress)
        val etPhone = dialogView.findViewById<EditText>(R.id.etCustomerPhone)
        val rgPayment = dialogView.findViewById<RadioGroup>(R.id.rgPaymentMethod)

        AlertDialog.Builder(requireContext())
            .setTitle("Complete Your Order")
            .setView(dialogView)
            .setCancelable(false)
            .setPositiveButton("Proceed") { _, _ ->
                val address = etAddress.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val isPrepaid = rgPayment.checkedRadioButtonId == R.id.rbPayNow

                if (address.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(requireContext(), "Please fill all details", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if(phone.length <10) {
                    Toast.makeText(requireContext(), "Please enter valid number!", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (isPrepaid) {
                    // Store details temporarily for Razorpay callback
                    tempItem = item
                    tempCartKey = cartKey
                    tempAddress = address
                    tempPhone = phone
                    startPayment(item, phone)
                } else {
                    finalizeOrder(item, address, phone, "COD", "Pending", cartKey)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun startPayment(item: CartData, phone: String) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_live_ILgsfZCZoFIKMb")

        try {
            val options = JSONObject()
            options.put("name", "Bakingo")
            options.put("description", "Payment for ${item.name}")
            options.put("theme.color", "#FF5722")
            options.put("currency", "INR")

            // Convert Price to Paisa
            val price = item.price?.toDoubleOrNull() ?: 0.0
            options.put("amount", (price * 100).toInt())

            options.put("prefill.contact", phone)
            options.put("prefill.email", auth.currentUser?.email ?: "customer@example.com")

            checkout.open(requireActivity(), options)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Razorpay Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun finalizeOrder(item: CartData, address: String, phone: String, method: String, status: String, cartKey: String) {
        val userId = auth.currentUser?.uid ?: return
        val orderRef = FirebaseDatabase.getInstance().getReference("Orders").push()
        val orderId = orderRef.key ?: ""

        val finalOrder = Order(
            orderId = orderId,
            userId = userId,
            itemName = item.name ?: "",
            itemPrice = item.price ?: "",
            itemWeight = item.weight ?: "",
            customerPhone = phone,
            deliveryAddress = address,
            paymentMethod = method,
            paymentStatus = status
        )

        orderRef.setValue(finalOrder).addOnSuccessListener {
            databaseRef.child(cartKey).removeValue() // Remove from cart
//            checkSMSPermissionAndSend(item)
            if (isAdded) Toast.makeText(requireContext(), "Order Placed Successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            if (isAdded) Toast.makeText(requireContext(), "Order Failed: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Razorpay Callbacks
    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        tempItem?.let {
            finalizeOrder(it, tempAddress, tempPhone, "Prepaid", "Paid", tempCartKey)
        }
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(requireContext(), "Payment Failed: $response", Toast.LENGTH_LONG).show()
    }

//    private fun checkSMSPermissionAndSend(item: CartData) {
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
//            sendDirectSMS(item)
//        } else {
//            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
//        }
//    }

//    private fun sendDirectSMS(item: CartData) {
//        val adminPhone = "7658848135"
//        val message = "New Order: ${item.name}, ₹${item.price}. User: ${auth.currentUser?.email}"
//        try {
//            val smsManager: SmsManager = requireContext().getSystemService(SmsManager::class.java)
//            smsManager.sendTextMessage(adminPhone, null, message, null, null)
//        } catch (e: Exception) {
//            debugPrint("SMS failed")
//        }
//    }

    private fun deleteItem(key: String) {
        databaseRef.child(key).removeValue()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Order(
    val orderId: String = "",
    val userId: String = "",
    val itemName: String = "",
    val itemPrice: String = "",
    val itemWeight: String = "",
    val customerPhone: String = "",
    val deliveryAddress: String = "",
    val paymentMethod: String = "", // "COD" or "Prepaid"
    val paymentStatus: String = "Pending",
    val timestamp: Long = System.currentTimeMillis()
)