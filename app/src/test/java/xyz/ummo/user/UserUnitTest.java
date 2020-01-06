package xyz.ummo.user;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import xyz.ummo.user.delegate.Login;
import xyz.ummo.user.delegate.User;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(JUnit4.class)
//@SmallTest
public class UserUnitTest {

    private Login login;
    private User user;

    @Before
    public void setLogin(){
        /*login = new Login() {
            @Override
            public void done(@NotNull byte[] data, @NotNull Number code) {

            }
        }*/
    }

    @Before
    public void setUser(){
        user = new User();
    }

    @Test
    public void login() {

        assertEquals(4, 2 + 2);
    }

    /**
     * Test for simple addition
     */
    /*@Test
    public void addTwoNumbers() {
        double resultAdd = mCalculator.add(1d, 1d);
        assertThat(resultAdd, is(equalTo(2d)));
        *//*
         *Assertions are expressions that must evaluate and result in true for the test to pass
         * Always use one method for a single assertion statement to avoid obscuring results
         **//*
    }*/
}