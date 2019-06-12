package hibernate.doc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.persistence.Persistence;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.query.Query;
import org.junit.Test;

import hibernate.doc.model.MyOrder;
import hibernate.doc.model.OrderItem;
import hibernate.doc.model.OrderItem_;
import hibernate.doc.model.Order_;
import hibernate.doc.model.Student;
import hibernate.doc.model.Student_;
import hibernate.doc.util.HibernateUtil;

public class CriteriaTest {

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

		// ʹ��tuple����������
		CriteriaBuilder build = session2.getCriteriaBuilder();
		CriteriaQuery<Tuple> query = build.createTupleQuery();
		Root<Student> root = query.from(Student.class);
		query.where(build.gt(root.get(Student_.age), 12));
		query.multiselect(root.get(Student_.age), root.get(Student_.name), root.get(Student_.address));
		List<Tuple> resultList = session2.createQuery(query).getResultList();

		for (Tuple tuple : resultList) {

			System.out.println(tuple.get(0) + "|" + tuple.get(root.get(Student_.name)) + "|" + tuple.get(2));
		}
		session2.close();
	}

	public static void prepareDataForOneToMany(Session session) {
		MyOrder order = new MyOrder();
		order.setOrderNumber("1");
		session.save(order);

		order = new MyOrder();
		order.setOrderNumber("2");
		OrderItem item = new OrderItem("beaf", 12);
		order.addOrderItem(item);
		item = new OrderItem("pork", 13);
		order.addOrderItem(item);
		session.save(order);

		order = new MyOrder();
		order.setOrderNumber("3");
		item = new OrderItem("apple", 9);
		order.addOrderItem(item);
		item = new OrderItem("pineapple", 15);
		order.addOrderItem(item);
		session.save(order);

		order = new MyOrder();
		order.setOrderNumber("4");
		item = new OrderItem("computer", 2);
		order.addOrderItem(item);
		item = new OrderItem("phone", 20);
		order.addOrderItem(item);
		session.save(order);

		item = new OrderItem("mouse", 23);
		session.save(item);
		item = new OrderItem("bottle", 50);
		session.save(item);
	}

	/**
	 * ���Զ��root,�Լ�������ʹ��
	 */
	// @Test
	public void testMutiRoot() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();
		CriteriaBuilder build = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> tuple = build.createTupleQuery();

		Root<OrderItem> orderItem = tuple.from(OrderItem.class);
		Root<MyOrder> order = tuple.from(MyOrder.class);
		// �������
		ParameterExpression<Integer> parameter = build.parameter(Integer.class, "count");

		Predicate preOrderItem = build.and(build.gt(orderItem.get(OrderItem_.count), parameter),
				build.isNotNull(orderItem.get(OrderItem_.order)));
		Predicate preOrder = build.and(build.isNotEmpty(order.get(Order_.orderItems)));
		tuple.where(build.and(preOrder, preOrderItem));
		tuple.multiselect(orderItem, order);
		Query<Tuple> query = session.createQuery(tuple);

		// ���ò���
		List<Tuple> resultList = query.setParameter("count", 15).getResultList();
		for (Tuple tuple2 : resultList) {
			OrderItem orderItem2 = tuple2.get(orderItem);
			MyOrder order2 = tuple2.get(order);
			System.out.println(orderItem2);
			System.out.println(order2);
			System.out.println();
		}
		session.close();
	}

	/**
	 * ����join
	 */
	// @Test
	public void testJoin() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();
		CriteriaBuilder build = session.getCriteriaBuilder();
		CriteriaQuery<Tuple> tuple = build.createTupleQuery();

		Root<MyOrder> order = tuple.from(MyOrder.class);

		ListJoin<MyOrder, OrderItem> join = order.join(Order_.orderItems);
		tuple.multiselect(order);
		tuple.where(build.isNotEmpty(order.get(Order_.orderItems)));
		tuple.distinct(true);
		Query<Tuple> query = session.createQuery(tuple);
		List<Tuple> resultList = query.getResultList();
		for (Tuple tuple2 : resultList) {

			MyOrder order2 = tuple2.get(order);

			System.out.println(order2);
			System.out.println();
		}
		session.close();
	}

	/**
	 * ����fetch
	 */

	// @Test
	public void testFetch() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();
		CriteriaBuilder build = session.getCriteriaBuilder();
		CriteriaQuery<MyOrder> query = build.createQuery(MyOrder.class);

		Root<MyOrder> order = query.from(MyOrder.class);

		Fetch<MyOrder, OrderItem> fetch = order.fetch(Order_.orderItems);

		// query.where(build.isNotEmpty(order.get(Order_.orderItems)));
		query.distinct(true);

		Query<MyOrder> fquery = session.createQuery(query);
		List<MyOrder> resultList = fquery.getResultList();

		for (MyOrder tuple2 : resultList) {

			System.out.println(tuple2);
			assertTrue(Persistence.getPersistenceUtil().isLoaded(tuple2.getOrderItem()));

			System.out.println();
		}
		session.close();
	}

	/**
	 * ����group by
	 */
	// @Test
	public void testGroupBy() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();
		CriteriaBuilder build = session.getCriteriaBuilder();
		CriteriaQuery<Object[]> query = build.createQuery(Object[].class);

		Root<OrderItem> orderItem = query.from(OrderItem.class);
		query.groupBy(orderItem.get(OrderItem_.order));
		query.select(build.array(build.sum(orderItem.get(OrderItem_.count)), orderItem.get(OrderItem_.order)));
		Query<Object[]> fquery = session.createQuery(query);
		List<Object[]> resultList = fquery.getResultList();

		for (Object[] objects : resultList) {

			System.out.println(objects[0]);
			System.out.println(objects[1]);

			System.out.println();
		}
		session.close();
	}

	/**
	 * projection��̽
	 */
	// @Test
	public void projectionFirst() {
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
		session = HibernateUtil.getSession();

		Criteria criteria = session.createCriteria(Student.class);
		Criteria projection = criteria.add(Restrictions.eq("age", 12)).setProjection(Projections.rowCount());
		int count = ((Long) projection.uniqueResult()).intValue();
		assertEquals(count, 1);
		session.close();
	}

	/**
	 * projection group by
	 */
	// @Test
	public void testLagecyGroupBy() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();
		Criteria criteria = session.createCriteria(OrderItem.class);
		// Ҫ�Թ���������Խ���groupby,��Ҫ����alias,���ﻹ��join�������������������û��order��OrderItem
		List<Object[]> list = criteria.createAlias("order", "o", Criteria.LEFT_JOIN)
				.setProjection(Projections.projectionList().add(Projections.rowCount())
						// �� sum(count)���ñ���,���ں���������
						// .add(Projections.alias(Projections.sum("count"), "cout"))
						// ���ñ����ĵڶ��ַ�ʽ
						// .add(Projections.sum("count"), "cout")
						// ���ñ����ĵ����ַ�ʽ
						.add(Projections.sum("count").as("cout"))

						// Ҳ����ʹ��Property������projection
						.add(Property.forName("product").max()).add(Projections.groupProperty("o.orderNumber")))
				// ���������˱�����cout

				.addOrder(Order.asc("cout")).list();

		for (Object[] objects : list) {
			System.out.println(objects[0] + "|" + objects[1] + "|" + objects[2] + "|" + objects[3]);
		}
		session.close();
	}

	/**
	 * ����DetachedCriteria
	 */
	// @Test
	public void detached() {
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
		session = HibernateUtil.getSession();
		DetachedCriteria stu = DetachedCriteria.forClass(Student.class);
		stu.add(Restrictions.gt("age", 13));
		Criteria query = stu.getExecutableCriteria(session);
		List<Student> list = query.list();
		for (Student student : list) {
			System.out.println(student);
		}
		session.close();
	}

	/**
	 * ����DetachedCriteria�Ӳ�ѯ
	 */
	// @Test
	public void detachedSubquery() {
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
		session = HibernateUtil.getSession();
		DetachedCriteria stu = DetachedCriteria.forClass(Student.class);

		stu.setProjection(Property.forName("age").avg().as("avgage"));

		Criteria criteria = session.createCriteria(Student.class);
		// ʹ��Property��detachedCriteria����Ӳ�ѯ,����restriction����
		criteria.add(Property.forName("age").gt(stu));
		List<Student> list = criteria.list();
		for (Student object : list) {
			System.err.println(object);
		}
		session.close();
	}

	/**
	 * �Ӳ�ѯ��
	 */
	// @Test
	public void detachedSubquery2() {
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
		session = HibernateUtil.getSession();
		DetachedCriteria stu = DetachedCriteria.forClass(Student.class);

		stu.setProjection(Property.forName("age"));

		Criteria criteria = session.createCriteria(Student.class);
		// �Ӳ�ѯ >=all
		criteria.add(Subqueries.propertyGeAll("age", stu));
		List<Student> list = criteria.list();
		for (Student object : list) {
			System.err.println(object);
		}
		session.close();
	}

	/**
	 * �Ӳ�ѯ��,����Ӳ�ѯ
	 */

	// @Test
	public void testRelatedSubquery() {
		Session session = HibernateUtil.getSession();
		Transaction tran = session.beginTransaction();
		prepareDataForOneToMany(session);
		tran.commit();
		session.close();

		session = HibernateUtil.getSession();
		DetachedCriteria sub = DetachedCriteria.forClass(OrderItem.class, "oisub")
				.setProjection(Property.forName("count").max())
				.add(Restrictions.eqProperty("oisub.order", "orderItem.order"));
		Criteria criteria = session.createCriteria(OrderItem.class, "orderItem");
		List<OrderItem> list = criteria.add(Property.forName("count").eq(sub)).list();
		for (OrderItem orderItem : list) {
			System.err.println(orderItem);
		}
		session.close();
	}

	/**
	 * ���бȽ��Ӳ�ѯ
	 */
	@Test
	public void mutiColumnSubquery() {
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
		session = HibernateUtil.getSession();
		DetachedCriteria stu = DetachedCriteria.forClass(Student.class);

		stu.setProjection(
				Projections.projectionList().add(Property.forName("age").max()).add(Property.forName("name").max()))

		;

		Criteria criteria = session.createCriteria(Student.class);
		// �Ӳ�ѯ >=all
		criteria.add(Subqueries.propertiesEq(new String[] { "age", "name" }, stu));
		List<Student> list = criteria.list();
		for (Student object : list) {
			System.err.println(object);
		}
		session.close();
	}

}
