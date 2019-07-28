package dasilva.marco.go4lunch;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import dasilva.marco.go4lunch.ui.main.Main;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class Go4LunchUnitTest {

    @Rule
    public ActivityTestRule<Main> mActivityTestRule = new ActivityTestRule<>(Main.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");


    SharedPreferences sharedPreferences;


    @Before
    public void setUp(){
        Context context = InstrumentationRegistry.getTargetContext();
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("choice", "Restaurant Aladin").apply();
    }


    @Test
    public void connectionWithFacebook() {
        onView(allOf(withId(R.id.fb_login_button), withText("Continuer avec Facebook"),
                childAtPosition(childAtPosition(withId(android.R.id.content),
                        0), 4), isDisplayed())).perform(click());
    }

    @Test
    public void connectWithGooGle() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(allOf(withText("Se connecter"), childAtPosition(allOf(withId(R.id.signInGoogle), childAtPosition(
                withClassName(is("android.support.constraint.ConstraintLayout")), 5)),
                0), isDisplayed())).perform(click());
    }

    @Test
    public void usersLoadedFromFirebaseDatabase() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.fb_login_button), withText("Continuer avec Facebook"),
                childAtPosition(childAtPosition(withId(android.R.id.content),
                        0), 4), isDisplayed())).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.workMatesItem), withContentDescription("Collègues"),
                childAtPosition(childAtPosition(withId(R.id.nav_bottom_maps),
                        0), 2), isDisplayed())).perform(click());

        onView(allOf(withId(R.id.item_list_name),
                childAtPosition(childAtPosition(withId(R.id.workmates_recyclerView), 1), 1), isDisplayed()));
    }


    @Test
    public void checkIfSelectedPlacesAreLoadedFromFirebaseDatabase() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

     onView(allOf(withId(R.id.fb_login_button), withText("Continuer avec Facebook"),
             childAtPosition(childAtPosition(withId(android.R.id.content),
                     0), 4), isDisplayed())).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

       onView(allOf(withId(R.id.listViewItem), withContentDescription("Liste"), childAtPosition(
               childAtPosition(withId(R.id.nav_bottom_maps), 0), 1),
                        isDisplayed())).perform(click());

        onView(allOf(withId(R.id.place_name), withText("Restaurant Aladin"),
                childAtPosition(childAtPosition(withId(R.id.list_of_places),
                        0), 0), isDisplayed())).check(matches(withText("Restaurant Aladin")));
    }

    @Test
    public void checkIfWorkMateIsDisplayedInListInDetailsActivityOnRecyclerviewItemClick(){
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.fb_login_button), withText("Continuer avec Facebook"),
                childAtPosition(childAtPosition(withId(android.R.id.content),
                        0), 4), isDisplayed())).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.listViewItem), withContentDescription("Liste"),
                childAtPosition(childAtPosition(withId(R.id.nav_bottom_maps),
                        0), 1), isDisplayed())).perform(click());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.place_name), withText("Restaurant Aladin"), childAtPosition(
                childAtPosition(withId(R.id.list_of_places), 0), 0),
                        isDisplayed())).check(matches(withText("Restaurant Aladin")));

       onView(allOf(withId(R.id.list_of_places), childAtPosition(withId(R.id.fragment_list_places),
               0))).perform(actionOnItemAtPosition(0, click()));

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

       onView(allOf(withId(R.id.item_details_name), withText("Marco participe"), childAtPosition(
               childAtPosition(withId(R.id.joinin_users_list), 0), 1),
                        isDisplayed())).check(matches(withText("Marco participe")));

    }

    @Test
    public void checkIfUserLunchDetailsIsDisplayedWithSuccess() {
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withText("Se connecter"), childAtPosition(
                allOf(withId(R.id.signInGoogle), childAtPosition(
                        withClassName(is("android.support.constraint.ConstraintLayout")),
                        5)), 0), isDisplayed())).perform(click());

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withContentDescription("Open navigation drawer"), childAtPosition(
                allOf(withId(R.id.toolbar), childAtPosition(
                        withClassName(is("android.support.design.widget.AppBarLayout")),
                        0)), 1), isDisplayed())).perform(click());

        onView(allOf(childAtPosition(allOf(withId(R.id.design_navigation_view),
                childAtPosition(withId(R.id.nav_view), 0)),
                        1), isDisplayed())).perform(click());

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.restaurant_details_info), withText("Restaurant Aladin"),
                childAtPosition(allOf(withId(R.id.restaurant_details_info_bar),
                        childAtPosition(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                1)), 0), isDisplayed())).check(matches(withText("Restaurant Aladin")));

        onView(allOf(withId(R.id.restaurant_details_adress), withText("103 Avenue de Paris"),
                childAtPosition(allOf(withId(R.id.restaurant_details_info_bar),
                        childAtPosition(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                                1)), 2), isDisplayed()));

       onView(allOf(withId(R.id.details_rating_bar), childAtPosition(allOf(withId(R.id.restaurant_details_info_bar),
               childAtPosition(IsInstanceOf.<View>instanceOf(android.view.ViewGroup.class),
                       1)), 1), isDisplayed()));
    }

    @Test
    public void checkIfWorkMatesRestaurantChoosed() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.fb_login_button), withText("Continuer avec Facebook"),
                childAtPosition(childAtPosition(withId(android.R.id.content),
                        0), 4), isDisplayed())).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.workMatesItem), withContentDescription("Collègues"),
                childAtPosition(childAtPosition(withId(R.id.nav_bottom_maps),
                        0), 2), isDisplayed())).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.item_list_name),
                childAtPosition(childAtPosition(withId(R.id.workmates_recyclerView), 1), 1), isDisplayed()))
                .check(matches(withText("Marco mange au Restaurant Aladin")));
    }

    @Test
    public void checkIfWorkMatesHasNotDecidesYet() {

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.fb_login_button), withText("Continuer avec Facebook"),
                childAtPosition(childAtPosition(withId(android.R.id.content),
                        0), 4), isDisplayed())).perform(click());

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.workMatesItem), withContentDescription("Collègues"),
                childAtPosition(childAtPosition(withId(R.id.nav_bottom_maps),
                        0), 2), isDisplayed())).perform(click());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(allOf(withId(R.id.item_list_name),
                childAtPosition(childAtPosition(withId(R.id.workmates_recyclerView), 0), 1), isDisplayed()))
                .check(matches(withHint("Band n'a pas encore choisi")));
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

}
