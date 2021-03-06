package io.katharsis.client;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.katharsis.client.mock.models.Task;
import io.katharsis.client.module.TestException;

public class ExceptionTest extends AbstractClientTest {

	protected ResourceRepositoryStub<Task, Long> taskRepo;

	@Before
	public void setup() {
		super.setup();
		taskRepo = client.getRepository(Task.class);
	}

	@Test
	public void test() {
		Task task = new Task();
		task.setId(10000L);
		task.setName("test");
		try {
			taskRepo.save(task);
			Assert.fail();
		}
		catch (TestException e) {
			Assert.assertEquals("msg", e.getMessage());
		}
	}
}