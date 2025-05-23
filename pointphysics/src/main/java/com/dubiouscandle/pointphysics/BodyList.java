package com.dubiouscandle.pointphysics;

import java.util.HashSet;
import java.util.Iterator;

public class BodyList implements Iterable<Body> {
	private int size = 0;
	private Body[] bodies = new Body[256];
	private HashSet<Body> toRemove = new HashSet<>();

	public BodyList() {
	}

	public void add(Body body) {
		if (size >= bodies.length) {
			Body[] newBodies = new Body[bodies.length * 2];
			System.arraycopy(bodies, 0, newBodies, 0, bodies.length);
			bodies = newBodies;
		}
		bodies[size++] = body;
	}

	public void queueRemove(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException(index);
		}
		toRemove.add(bodies[index]);
	}

	public void queueRemove(Body body) {
		toRemove.add(body);
	}

	public void flushRemove() {
		for (int i = size - 1; i >= 0; i--) {
			if (toRemove.remove(bodies[i])) {
				size--;
				bodies[i] = bodies[size];
				bodies[size] = null;
			}
		}

		if (toRemove.size() != 0) {
			throw new IllegalStateException("Bodies not found: " + toRemove);
		}
		
		toRemove.clear();
	}

	public Body get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException(index);
		}
		return bodies[index];
	}

	public int size() {
		return size;
	}

	@Override
	public Iterator<Body> iterator() {
		return new Iterator<Body>() {
			private int i = 0;

			@Override
			public boolean hasNext() {
				return i < size;
			}

			@Override
			public Body next() {
				if (i == size) {
					throw new IllegalStateException("No more elements.");
				}

				Body body = bodies[i];
				i++;
				return body;
			}
		};
	}
}
