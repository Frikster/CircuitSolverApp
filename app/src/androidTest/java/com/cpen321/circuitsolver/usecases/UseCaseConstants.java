package com.cpen321.circuitsolver.usecases;

import com.cpen321.circuitsolver.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Cornelis Dirk Haupt on 12/12/2016.
 * For further development additional circuits, components etc  * used for testing across use cases
 * may be defined here
 */
public final class UseCaseConstants {
    // General circuits not designed to comply with any assumptions
    public static final List<Integer> TEST_CIRCUITS =
            Collections.unmodifiableList(Arrays.asList(
                    R.drawable.example_1,
                    R.drawable.example_2
                    /* add additional test circuits here */));


    // Test circuits designed to comply with Use Case 1 input assumptions
    public static final List<Integer> TEST_CIRCUITS_UC1 =
            Collections.unmodifiableList(Arrays.asList(
                    R.drawable.example_1
                    /* add additional test circuits here.
                    Note that we see no need to test multiple circuits for UC1
                     So you'll have to add loops to the code as well*/));
}
