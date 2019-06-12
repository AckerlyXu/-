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
	 * �򵥵�rawSql
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

		// ͶӰ
//		List<Object[]> list = session2.createNativeQuery("select * from student").addScalar("id", IntegerType.INSTANCE)
//				.addScalar("name", org.hibernate.type.StringType.INSTANCE).list();
//
//		for (Object[] objects : list) {
//			System.out.println(objects[0] + "|" + objects[1] + objects.length);
//		}

		// ����һ��ʵ������б� id,name,age,addressҲ������*����
		NativeQuery<Student> students = session2.createNativeQuery("select  id,name,age,address from student")
				.addEntity(Student.class);
		// ��������
		// .createNativeQuery("select * from student", Student.class);

		for (Student student : students.getResultList()) {
			System.out.println(student);
		}
		// ʹ��TransformersͶӰ����һ������hibernate�����ʵ��
		// ��rawSql��Ҳ��ָ������
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
		// ����Ƕ��һ,����ָ�������
		List<OrderItem> list = session.createNativeQuery("select id,count,product,order_id from orderitem")
				.addEntity(OrderItem.class).getResultList();
		for (OrderItem tuple2 : list) {

			System.out.println(tuple2);

			System.out.println();
		}

		// �����һ�Զ�,��ôΪ�˻�ö����һ��,��Ҫʹ�����Ӳ�ѯ,��Ȼ���һ�����ǿյ�
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
	 * ���� ManyToOne
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

		// Ĭ��session
		// addjoinҲ�᷵�ع�����ʵ��,���Է���ֵ�����Ƕ�������ļ���,���ֻ��Ҫ���ص���ʵ��,����ʹ��setResultTransformer
		// һ�Զ�������,root_entityָ����һ����һ��
		// ������һ�������,���ص�һ�ļ����������ص�,��ʹʹ�õ������Ӳ�ѯ
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
			// ��һ���Ӷ�������,һ�Ķ��Ѿ��Ǽ�����ϵ�
			assertTrue(Persistence.getPersistenceUtil().isLoaded(((MyOrder) tuple2[0]).getOrderItem()));

			System.out.println(tuple2[0]);
			System.out.println(tuple2[1]);
			System.out.println();
		}

		// һ���Ӷ�������Root_Entityָ���Ƕ�
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
	 * ���Է�ֹ���ű�������ͬ������
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
			// ��һ���Ӷ�������,һ�Ķ��Ѿ��Ǽ�����ϵ�
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
	 * ʹ��namedRawQuery
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
	 * ���Է��ض��ʵ���namedQuery
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
			// ��һ���Ӷ�������,һ�Ķ��Ѿ��Ǽ�����ϵ�
			// assertTrue(Persistence.getPersistenceUtil().isLoaded(((MyOrder)
			// tuple2[0]).getOrderItem()));

			System.out.println(tuple2[0]);
			System.out.println(tuple2[1]);
			System.out.println();
		}

		session.close();
	}

	/**
	 * ʹ�ô洢����
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
		// ��Out�����Ĵ洢����
		ProcedureCall query = session2.createStoredProcedureCall("sp_student");
		query.registerParameter("inage", Integer.class, ParameterMode.IN);
		query.registerParameter("total", Integer.class, ParameterMode.OUT);
		query.setParameter("inage", 14);

		query.execute();
		Object out = query.getOutputParameterValue("total");
		System.out.println(out);
		// ֱ�ӻ�ý����
		ProcedureCall query2 = session2.createStoredProcedureCall("sp__select_student", Student.class);
		// Ҳ������bindValue�󶨲���
		query2.registerParameter("inage", Integer.class, ParameterMode.IN).bindValue(14);
		;
		// query2.setParameter("inage", 14);

		// ��ý����
		List resultList = query2.getResultList();
		for (Object object : resultList) {
			System.out.println(object);
		}
		// Ҳ����������ý����
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
