package com.rasalexman.flairframework.interfaces

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.rasalexman.flairframework.ext.className
import java.util.*

/**
 * Created by a.minkin on 21.11.2017.
 */
interface IMediator : INotifier {
    /**
     * Current view for IMediator instance
     */
    var viewComponent: View?
    /**
     * Does options menu activated
     */
    var hasOptionalMenu: Boolean
    /**
     * If we need to hide options menu
     */
    var hideOptionalMenu: Boolean
    /**
     * Does IMedaitor `viewComponent` added to root container
     */
    var isAdded: Boolean
    /**
     * Does IMediator `viewComponent` was destroyed
     */
    var isDestroyed: Boolean
    /**
     * Current IMediator name for retrieving
     */
    var mediatorName: String?

    /**
     * Bundle arguments
     */
    val arguments: Bundle
    /**
     * List of notification names that this IMediator is listening for
     */
    val listNotificationInterests: ArrayList<String>

    /**
     * When current View has attached your menu
     */
    fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater)

    /**
     * Fragment legacy menu creation
     */
    fun onPrepareOptionsMenu(menu: Menu)

    /**
     * Menu Item Selection section
     */
    fun onOptionsItemSelected(item: MenuItem): Boolean

    /**
     * Respond function from current activity
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    /**
     * WHEN REQUESTED PERMISSIONS IS GRANTED
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)

    /**
     * Main initialize ViewComponent Function
     */
    fun createLayout(context: Context): View

    /**
     * It's called before view created
     * If we need to prepare some things before view is created ex get saved params from bundle
     */
    fun onPrepareCreateView()

    /**
     * Called by the create mediator view once
     */
    fun onCreatedView(view: View)

    /**
     * Called when mediator added to view container
     */
    fun onAddedView(view: View)

    /**
     * Called when mediator view removed from parent
     */
    fun onRemovedView(view: View)

    /**
     * When view is destroyed or equal to null
     */
    fun onDestroyView()


    /**
     * Called by the View when the Mediator is registered.
     */
    fun onRegister()

    /**
     * Called by the View when the Mediator is removed.
     */
    fun onRemove()

    /**
     * Handle the hardware back button
     */
    fun handleBackButton(animation: IAnimator? = null): Boolean
}

/**
 * Inflate current view component
 */
fun IMediator.inflateView(layoutId: Int): View = android.view.LayoutInflater.from(activity).inflate(layoutId, null)

/**
 * Start Activity with given intant and request code
 */
fun IMediator.startActivityForResult(intent: Intent, requestCode: Int, options: Bundle? = null) {
    facade.view.startActivityForResult(intent, requestCode, options)
}

/**
 * Start another activity with bundle options or not
 */
fun IMediator.startActivity(intent: Intent, bundle:Bundle? = null) {
    bundle?.let {
        activity.startActivity(intent, it)
    } ?: activity.startActivity(intent)
}

/**
 * Request Permissions from user
 */
fun IMediator.requestPermissions(permissions: Array<String>, requestCode: Int) {
    facade.view.requestPermissions(permissions, requestCode)
}

/**
 * Check the given permission to be approved by user or system
 */
fun IMediator.checkSelfPermission(permissionToCheck: String): Int = facade.view.checkSelfPermission(permissionToCheck)

/**
 * Should we show permission description dialog for user
 */
fun IMediator.shouldShowRequestPermissionRationale(permission: String): Boolean = facade.view.shouldShowRequestPermissionRationale(permission)


/**
 * Register a list of `INotification` interests.
 *
 * @param listNotification
 * array of string notification names
 *
 * @param notificator
 * the callback function
 *
 */
fun IMediator.registerListObservers(listNotification: List<String>, notificator: INotificator): IMediator {
    listNotification.forEach {
        registerObserver(it, notificator)
    }
    return this
}

/**
 * Register observer function with notifName
 *
 * @param notifName
 * Name of notification that recieve notificator
 *
 * @param notificator
 * This is simple closure function callback
 *
 */
fun IMediator.registerObserver(notifName: String, notificator: INotificator): IMediator {
    listNotificationInterests.add(notifName)
    facade.registerObserver(notifName, notificator)
    return this
}

/**
 * Remove observer by given notification name
 *
 * @param notifName
 * Name of notification that recieve notificator
 */
fun IMediator.removeObserver(notifName: String) {
    listNotificationInterests.remove(notifName)
    facade.removeObserver(notifName, this.multitonKey)
}

/**
 * Remove all notifications from current IMediator implementation instance
 */
fun IMediator.removeAllObservers() {
    listNotificationInterests.forEach {
        facade.removeObserver(it, this.multitonKey)
    }
    listNotificationInterests.clear()
}

/**
 * Add current mediator to view stage
 *
 * @param popLast
 * Flag that indicates to need remove last showing mediator from backstack
 */
fun IMediator.show(animation: IAnimator? = null, popLast: Boolean = false) {
    facade.showMeditator<IMediator>(this.mediatorName ?: this.className(), popLast, animation)
}

/**
 * Remove current mediator from view stage
 *
 * @param animation
 * Current hide animation
 *
 * @param popIt
 * Flag that indicates to need remove current mediator from backstack
 */
fun IMediator.hide(animation: IAnimator? = null, popIt: Boolean = false) {
    facade.hideMediator(this.mediatorName ?: this.className(), popIt, animation)
}

/**
 * Get app context resources
 */
fun IMediator.resources(): Resources {
    return appContext.resources
}

/**
 * Hide current mediator and remove it from backstack
 * Then show last added mediator from backstack
 *
 * @param animation
 * Current hiding animation for pop
 */
fun IMediator.popToBack(animation: IAnimator? = null) {
    facade.popMediator(this.mediatorName ?: this.className(), animation)
}

/**
 * Retrieve lazy mediator core by given generic class
 *
 * @param mediatorName
 * Mediator name to be retrieved by lazy function
 */
inline fun <reified T : IMediator> IMediator.mediatorLazy(mediatorName: String? = null): Lazy<T> = lazy {
    mediator<T>(mediatorName)
}

/**
 * Retrieve lazy mediator core by given generic class
 *
 * @param mediatorName
 * Mediator name to be retrieved
 */
inline fun <reified T : IMediator> IMediator.mediator(mediatorName: String? = null): T = facade.retrieveMediator(mediatorName)


/**
 * GO Back to given mediatorName(high priority) or Generic class name.
 *
 * @param mediatorName
 * Mediator to which you go back, so if there is no registered mediatorLazy by this name new instance is create as back stacked
 *
 * @param animation
 * Current hiding animation for pop
 */
inline fun <reified T : IMediator> IMediator.popTo(mediatorName: String? = null, animation: IAnimator? = null) {
    facade.retrieveMediator<T>(mediatorName ?: T::class.className()).show(animation, true)
}

/**
 * Show the given generic mediatorLazy or by name
 *
 * @param mediatorName
 * Mediator name to be showed
 */
inline fun <reified T : IMediator> IMediator.showMediator(mediatorName: String? = null, animation: IAnimator? = null) {
    return facade.retrieveMediator<T>(mediatorName ?: T::class.className()).show(animation)
}


////------ SOME USEFUL EXTENSION FUNCTIONS
/**
 * Find the view in `viewComponent` by given resource Id
 *
 * @param resId
 * Resource view identificator like `R.id.my_button_id`
 */
inline fun <reified T : View> IMediator.view(resId: Int): Lazy<T?> = lazy {
    viewComponent?.findViewById(resId) as? T
}

/**
 * Get a string resourses from app context by it id
 */
fun INotifier.string(resId: Int): String {
    return appContext.getString(resId)
}

/**
 * Drawable from resource id
 */
fun INotifier.drawable(resource: Int): Drawable? = ContextCompat.getDrawable(appContext, resource)

/***
 * Custom View For somethings like rounded drawable
 * */
fun roundedDrawable(col:Int, corners:Float = 100f, withStroke:Boolean = false, strokeColor:Int = Color.LTGRAY, strokeWeight:Int = 2) = GradientDrawable().apply {
    shape = GradientDrawable.RECTANGLE
    cornerRadius = corners
    setColor(col)
    if(withStroke) setStroke(strokeWeight, strokeColor)
}

/**
 * Color from resources id
 */
fun INotifier.color(resource: Int): Int = ContextCompat.getColor(appContext, resource)

/**
 * For adaptive procent width
 */
fun INotifier.wdthProc(proc:Float):Int = (Resources.getSystem().displayMetrics.widthPixels*proc).toInt()

/**
 * For adaptive procent height
 */
fun INotifier.hdthProc(proc:Float):Int = (Resources.getSystem().displayMetrics.heightPixels*proc).toInt()







