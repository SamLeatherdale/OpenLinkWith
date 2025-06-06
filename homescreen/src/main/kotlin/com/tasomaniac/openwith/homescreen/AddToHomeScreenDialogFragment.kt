package com.tasomaniac.openwith.homescreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.tasomaniac.android.widget.DelayedProgressBar
import com.tasomaniac.openwith.extensions.toast
import com.tasomaniac.openwith.resolver.DisplayActivityInfo
import com.tasomaniac.openwith.translations.R.string
import com.tasomaniac.openwith.util.Intents
import dagger.android.support.DaggerAppCompatDialogFragment
import timber.log.Timber
import javax.inject.Inject

class AddToHomeScreenDialogFragment : DaggerAppCompatDialogFragment() {

    @Inject lateinit var titleFetcher: TitleFetcher

    private lateinit var shortcutIconCreator: ShortcutIconCreator
    private lateinit var titleView: EditText
    private lateinit var progressBar: DelayedProgressBar

    private val activityToAdd: DisplayActivityInfo
        get() = requireArguments().getParcelable(KEY_ACTIVITY_TO_ADD)!!
    private val intent: Intent
        get() = requireArguments().getParcelable(KEY_INTENT)!!

    private val positiveButton: Button
        get() = (dialog as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE)

    override fun onStart() {
        super.onStart()
        onTitleChanged(titleView.text)
        shortcutIconCreator = ShortcutIconCreator(BitmapFactory.decodeResource(resources, R.drawable.ic_bookmark))

        if (titleView.text.isEmpty()) {
            showProgressBar()
            titleFetcher.fetch(
                intent.dataString!!,
                { title ->
                    hideProgressBar()
                    if (title != null && titleView.text.isEmpty()) {
                        titleView.post {
                            titleView.append(title)
                        }
                    }
                },
                ::hideProgressBar
            )
        }

        titleView.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_GO -> {
                    createShortcutAndHandleError()
                    true
                }
                else -> false
            }
        }

        titleView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable) = onTitleChanged(text)
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        })
    }

    override fun onDestroy() {
        titleFetcher.cancel()
        super.onDestroy()
    }

    private fun showProgressBar() {
        progressBar.post { progressBar.show() }
    }

    private fun hideProgressBar() {
        progressBar.post { progressBar.hide(true) }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        @SuppressLint("InflateParams")
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_add_to_home_screen, null)
        titleView = view.findViewById(R.id.add_to_home_screen_title)
        progressBar = view.findViewById(R.id.add_to_home_screen_progress)

        return AlertDialog.Builder(requireContext())
            .setPositiveButton(string.add) { _, _ -> createShortcutAndHandleError() }
            .setNegativeButton(string.cancel, null)
            .setView(view)
            .setTitle(string.add_to_homescreen)
            .create()
            .also { forceKeyboardVisible(it.window!!) }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().finish()
    }

    fun onTitleChanged(title: CharSequence) {
        positiveButton.isEnabled = title.isNotEmpty()
    }

    private fun createShortcutAndHandleError() {
        val success = createShortcut()
        if (!success) {
            requireContext().toast(string.add_to_home_screen_error, Toast.LENGTH_SHORT)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun createShortcut(): Boolean {
        val id = intent.dataString!! + activityToAdd.packageName()
        val label = titleView.text.toString()

        fun createShortcutWith(icon: IconCompat): Boolean {
            val shortcut = ShortcutInfoCompat.Builder(requireContext(), id)
                .setIntent(intent)
                .setShortLabel(label)
                .setIcon(icon)
                .build()
            return ShortcutManagerCompat.requestPinShortcut(requireContext(), shortcut, startHomeScreen())
        }

        return try {
            activityToAdd.displayIcon?.let {
                createShortcutWith(shortcutIconCreator.createIconFor(it))
            } ?: createShortcutWith(createSimpleIcon())
        } catch (e: Exception) {
            // This method started to fire android.os.TransactionTooLargeException
            Timber.e(e, "Exception while adding shortcut")
            createShortcutWith(createSimpleIcon())
        }
    }

    private fun createSimpleIcon(): IconCompat =
        IconCompat.createWithResource(requireContext(), R.mipmap.ic_launcher_bookmark)

    private fun startHomeScreen(): IntentSender =
        PendingIntent.getActivity(
            requireContext(),
            0,
            Intents.homeScreenIntent(),
            PendingIntent.FLAG_IMMUTABLE
        ).intentSender

    fun show(fragmentManager: FragmentManager) = show(fragmentManager, "AddToHomeScreen")

    companion object {

        private const val KEY_ACTIVITY_TO_ADD = "activity_to_add"
        private const val KEY_INTENT = "intent"

        @JvmStatic
        fun newInstance(activityInfo: DisplayActivityInfo, intent: Intent): AddToHomeScreenDialogFragment {
            val homeScreenIntent = Intent(Intent.ACTION_VIEW).apply {
                data = intent.data
                component = intent.component
            }
            return AddToHomeScreenDialogFragment().apply {
                arguments = bundleOf(
                    KEY_ACTIVITY_TO_ADD to activityInfo,
                    KEY_INTENT to homeScreenIntent
                )
            }
        }

        private fun forceKeyboardVisible(window: Window) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }
}
