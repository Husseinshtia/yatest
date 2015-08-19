package com.nyaschenko.categories;

import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class CategoryActivity extends AppCompatActivity implements CategoryFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Fragment fragment = getFragmentManager().findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, CategoryFragment.newInstance(-1, getString(R.string.app_name)))
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onFragmentInteraction(long id, String title) {
        Log.d("CATEGORY", "fragment clicked: " + id);
        Fragment fragment = CategoryFragment.newInstance(id, title);
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 1) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            LoadService.startActionLoadCategories(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
