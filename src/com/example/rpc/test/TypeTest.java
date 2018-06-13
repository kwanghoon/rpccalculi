package com.example.rpc.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.example.rpc.Location;

class TypeTest {

	@Test
	void test() {
		assertTrue(Location.Client.equals(Location.Client));
	}

}
