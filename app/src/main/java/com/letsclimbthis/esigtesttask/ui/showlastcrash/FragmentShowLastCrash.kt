package com.letsclimbthis.esigtesttask.ui.showlastcrash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.letsclimbthis.esigtesttask.databinding.FragmentShowLastCrashBinding
import com.letsclimbthis.esigtesttask.logMessage
import com.letsclimbthis.esigtesttask.ui.utils.displayMessage
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.util.logging.Logger

class FragmentShowLastCrash: Fragment() {

    private var _binding: FragmentShowLastCrashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowLastCrashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkForCrash()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkForCrash() {
        var trace = ""
        var log = ""
        try {
            val reader = BufferedReader(InputStreamReader(requireActivity().openFileInput("stack.trace")))
            trace = reader.use(BufferedReader::readText)

            log = if (logMessage.isNotEmpty()) logMessage
            else {
                val reader2 = BufferedReader(InputStreamReader(requireActivity().openFileInput("log")))
                reader2.use(BufferedReader::readText)
            }
        }
        catch(e: FileNotFoundException) {
            displayMessage(e.toString())
        }
        catch(e: IOException) {
            displayMessage(e.toString())
        }
        if (trace.isEmpty() && log.isEmpty()) displayMessage("There haven't been any exception yet")
        else {
            val result = log + trace
            binding.tvShowLastCrash.text = result
        }
    }
}