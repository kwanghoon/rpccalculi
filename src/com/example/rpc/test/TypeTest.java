package com.example.rpc.test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.example.rpc.Location;

class TypeTest {

	@Test
	void test() {
		assertTrue(Location.Client.equals(Location.Client));
	}

}
