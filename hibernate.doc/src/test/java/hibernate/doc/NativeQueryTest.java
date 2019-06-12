package hibernate.doc;

import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.ParameterMode;
import javax.persistence.Persistence;
import javax.persistence.StoredProcedureQuery;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.procedure.ProcedureCall;
import org.hibernate.query.NativeQuery;
import org.hibernate.result.Output;
import org.hibernate.result.ResultSetOutput;
import org.hibernate.transform.Transformers;
import org.junit.Test;

import hibernate.doc.model.MyOrder;
import hibernate.doc.model.OrderItem;
import hibernate.doc.model.Student;
import hibernate.doc.model.StudentDTO;
import hibernate.doc.util.HibernateUtil;

public class NativeQueryTest {

	/**
	 * 简单的rawSql
	 */
	// @Test
	public void test() {
		Session session = HibernateUtil.getSession();

		Transaction tran = session.beginTransaction();
		for (int i = 0; i < 10; i++) {
			Student student = new Student();
			student.setAddress("addr" + i);
			student.setAge(10 + i);
			student.setName("stu" + i);
			session.save(student);
		}
		tran.commit();
		session.close();
		Session session2 = HibernateUtil.getSession();

		// 投影
//		List<Object[]> list = session2.createNativeQuery("select * from student").addScalar("id", IntegerType.INSTANCE)
//				.addScalar("name", org.hibernate.type.StringType.INSTANCE).list();
//
//		for (Object[] objects : list) {
//			System.out.println(objects[0] + "|" + objects[1] + objects.length);
//		}

		// 返回一个实体对象列表 id,name,age,address也可以用*代替
		NativeQuery<Student> students = session2.createNativeQuery("select  id,name,age,address from student")
				.addEntity(Student.class);
		// 或者这样
		// .createNativeQuery("select * from student", Student.class);

		for (Student student : students.getResultList()) {
			System.out.println(student);
		}
		// 使用Transformers投影到另一个不被hibernate管理的实体
		// 在rawSql中也能指定参数
		List<StudentDTO> dtos = session2.createNativeQuery("select * from student where student.age>:age")
				.setResultTransformer(Transformers.aliasToBean(StudentDTO.class)).setParameter("age", 14)
				.getResultList();
		for (StudentDTO studentDTO : dtos) {
			System.out.println(studentDTO);
		}
		session2.close();
	}

//	@Test
	public void testManyToOne() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		CriteriaTest.prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();
		// 如果是多对一,必须指定外键列
		List<OrderItem> list = session.createNativeQuery("select id,count,product,order_id from orderitem")
				.addEntity(OrderItem.class).getResultList();
		for (OrderItem tuple2 : list) {

			System.out.println(tuple2);

			System.out.println();
		}

		// 如果是一对多,那么为了获得多的那一方,需要使用连接查询,不然多的一方会是空的
		List<MyOrder> list2 = session.createNativeQuery(
				"select * from `order` join orderitem on `order`.id = orderitem.order_id", MyOrder.class).list();
		for (MyOrder order : list2) {
			System.out.println(order);

			// assertFalse(Persistence.getPersistenceUtil().isLoaded(order.getOrderItem()));
			System.out.println(order.getOrderItem().get(0));
			;
		}
		// assertFalse(Persistence.getPersistenceUtil().isLoaded(list.get(1).getOrder()));
		session.close();
	}

	/**
	 * 测试 ManyToOne
	 */
	// @Test
	public void testJoin() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		CriteriaTest.prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();

		List<Object[]> list = session
				.createNativeQuery("select * from   orderitem oi join `order` o on oi.order_id= o.id ")
				.addEntity("orderitem", OrderItem.class).addJoin("o", "orderitem.order").list();
		for (Object[] tuple2 : list) {

			System.out.println(tuple2[0]);
			System.out.println(tuple2[1]);
			System.out.println();
		}

		// 默认session
		// addjoin也会返回关联的实体,所以返回值类型是对象数组的集合,如果只需要返回单个实体,可以使用setResultTransformer
		// 一对多的情况下,root_entity指的是一的那一方
		// 多连接一的情况下,返回的一的集合是懒加载的,即使使用的是连接查询
		List<MyOrder> list1 = session
				.createNativeQuery("select * from   orderitem oi join `order` o on oi.order_id= o.id ")
				.addEntity("orderitem", OrderItem.class).addJoin("o", "orderitem.order")
				.setResultTransformer(Criteria.ROOT_ENTITY).list();
		for (MyOrder tuple2 : list1) {

			System.out.println(tuple2);

			System.out.println();
		}
		// assertFalse(Persistence.getPersistenceUtil().isLoaded(list.get(1).getOrder()));
		session.close();
	}

//	@Test
	public void testJoin2() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		CriteriaTest.prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();

		List<Object[]> list = session
				.createNativeQuery("select * from  myorder o join orderitem oi on  o.id= oi.`order_id`  ")
				.addEntity("order", MyOrder.class).addJoin("oi", "order.orderItems").list();
		for (Object[] tuple2 : list) {
			// 在一连接多的情况下,一的多已经是加载完毕的
			assertTrue(Persistence.getPersistenceUtil().isLoaded(((MyOrder) tuple2[0]).getOrderItem()));

			System.out.println(tuple2[0]);
			System.out.println(tuple2[1]);
			System.out.println();
		}

		// 一连接多的情况下Root_Entity指的是多
		List<OrderItem> list2 = session
				.createNativeQuery("select * from  myorder o join orderitem oi on  o.id= oi.`order_id`  ")
				.addEntity("order", MyOrder.class).addJoin("oi", "order.orderItems")
				.setResultTransformer(Criteria.ROOT_ENTITY).list();
		for (OrderItem myOrder : list2) {
			System.out.println(myOrder);

			assertTrue(Persistence.getPersistenceUtil().isLoaded(myOrder.getOrder()));
		}
		session.close();
	}

	/**
	 * 测试防止两张表中有相同的列名
	 */

//	@Test
	public void testConflict() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		CriteriaTest.prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();

		List<Object[]> list = session
				.createNativeQuery("select {o.*},{oi.*} from  myorder o join orderitem oi on  o.id= oi.`order_id`  ")
				.addEntity("o", MyOrder.class).addEntity("oi", OrderItem.class).list();
		for (Object[] tuple2 : list) {
			// 在一连接多的情况下,一的多已经是加载完毕的
			// assertTrue(Persistence.getPersistenceUtil().isLoaded(((MyOrder)
			// tuple2[0]).getOrderItem()));
			System.out.println(((MyOrder) tuple2[0]).getOrderItem().size());
			System.out.println(tuple2[0]);
			System.out.println(tuple2[1]);
			System.out.println();
		}

		session.close();
	}

	/**
	 * 使用namedRawQuery
	 */

	// @Test
	public void testNamedQuery() {
		Session session = HibernateUtil.getSession();

		Transaction tran = session.beginTransaction();
		for (int i = 0; i < 10; i++) {
			Student student = new Student();
			student.setAddress("addr" + i);
			student.setAge(10 + i);
			student.setName("stu" + i);
			session.save(student);
		}
		tran.commit();
		session.close();
		Session session2 = HibernateUtil.getSession();

		List<StudentDTO> dtos = session2.createNamedQuery("student").setParameter("age", 14).getResultList();
		System.out.println(dtos.size());
		for (StudentDTO studentDTO : dtos) {
			System.out.println(studentDTO);
		}
		session2.close();
	}

	/**
	 * 测试返回多个实体的namedQuery
	 */
	// @Test
	public void testNames() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		CriteriaTest.prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();

		List<Object[]> resultList = session.createNamedQuery("order").getResultList();
		for (Object[] tuple2 : resultList) {
			// 在一连接多的情况下,一的多已经是加载完毕的
			// assertTrue(Persistence.getPersistenceUtil().isLoaded(((MyOrder)
			// tuple2[0]).getOrderItem()));

			System.out.println(tuple2[0]);
			System.out.println(tuple2[1]);
			System.out.println();
		}

		session.close();
	}

	/**
	 * 使用存储过程
	 */
	@Test
	public void testStoredProcedure() {
		Session session = HibernateUtil.getSession();

		Transaction tran = session.beginTransaction();
		for (int i = 0; i < 10; i++) {
			Student student = new Student();
			student.setAddress("addr" + i);
			student.setAge(10 + i);
			student.setName("stu" + i);
			session.save(student);
		}
		tran.commit();
		session.close();
		Session session2 = HibernateUtil.getSession();
		// 带Out参数的存储过程
		ProcedureCall query = session2.createStoredProcedureCall("sp_student");
		query.registerParameter("inage", Integer.class, ParameterMode.IN);
		query.registerParameter("total", Integer.class, ParameterMode.OUT);
		query.setParameter("inage", 14);

		query.execute();
		Object out = query.getOutputParameterValue("total");
		System.out.println(out);
		// 直接获得结果集
		ProcedureCall query2 = session2.createStoredProcedureCall("sp__select_student", Student.class);
		// 也可以用bindValue绑定参数
		query2.registerParameter("inage", Integer.class, ParameterMode.IN).bindValue(14);
		;
		// query2.setParameter("inage", 14);

		// 获得结果集
		List resultList = query2.getResultList();
		for (Object object : resultList) {
			System.out.println(object);
		}
		// 也可以这样获得结果集
		Output output = query2.getOutputs().getCurrent();
		List resultList1 = ((ResultSetOutput) output).getResultList();
		for (Object object : resultList1) {
			System.out.println(object);
		}
		StoredProcedureQuery namedStored = session2.createNamedStoredProcedureQuery("stu_pro");
		namedStored.setParameter("inage", 12);
		namedStored.execute();
		Object outputParameterValue = namedStored.getOutputParameterValue("total");
		System.out.println(outputParameterValue);
		session2.close();
	}

}
