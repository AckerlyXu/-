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

		// 使用tuple保存多个数据
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
	 * 测试多个root,以及参数的使用
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
		// 定义参数
		ParameterExpression<Integer> parameter = build.parameter(Integer.class, "count");

		Predicate preOrderItem = build.and(build.gt(orderItem.get(OrderItem_.count), parameter),
				build.isNotNull(orderItem.get(OrderItem_.order)));
		Predicate preOrder = build.and(build.isNotEmpty(order.get(Order_.orderItems)));
		tuple.where(build.and(preOrder, preOrderItem));
		tuple.multiselect(orderItem, order);
		Query<Tuple> query = session.createQuery(tuple);

		// 设置参数
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
	 * 测试join
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
	 * 测试fetch
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
	 * 测试group by
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
	 * projection初探
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
		// 要对关联表的属性进行groupby,需要设置alias,这里还对join的类型作了限制来获得没有order的OrderItem
		List<Object[]> list = criteria.createAlias("order", "o", Criteria.LEFT_JOIN)
				.setProjection(Projections.projectionList().add(Projections.rowCount())
						// 给 sum(count)设置别名,以在后面引用它
						// .add(Projections.alias(Projections.sum("count"), "cout"))
						// 设置别名的第二种方式
						// .add(Projections.sum("count"), "cout")
						// 设置别名的第三种方式
						.add(Projections.sum("count").as("cout"))

						// 也可以使用Property来设置projection
						.add(Property.forName("product").max()).add(Projections.groupProperty("o.orderNumber")))
				// 引用设置了别名的cout

				.addOrder(Order.asc("cout")).list();

		for (Object[] objects : list) {
			System.out.println(objects[0] + "|" + objects[1] + "|" + objects[2] + "|" + objects[3]);
		}
		session.close();
	}

	/**
	 * 测试DetachedCriteria
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
	 * 测试DetachedCriteria子查询
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
		// 使用Property和detachedCriteria表达子查询,这里restriction不行
		criteria.add(Property.forName("age").gt(stu));
		List<Student> list = criteria.list();
		for (Student object : list) {
			System.err.println(object);
		}
		session.close();
	}

	/**
	 * 子查询二
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
		// 子查询 >=all
		criteria.add(Subqueries.propertyGeAll("age", stu));
		List<Student> list = criteria.list();
		for (Student object : list) {
			System.err.println(object);
		}
		session.close();
	}

	/**
	 * 子查询三,相关子查询
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
	 * 多列比较子查询
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
		// 子查询 >=all
		criteria.add(Subqueries.propertiesEq(new String[] { "age", "name" }, stu));
		List<Student> list = criteria.list();
		for (Student object : list) {
			System.err.println(object);
		}
		session.close();
	}

}
