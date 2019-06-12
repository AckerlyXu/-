package hibernate.doc.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class OrderItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String product;
	private int count;
	@ManyToOne(fetch = FetchType.LAZY)
	private MyOrder order;

	public String getProduct() {
		return product;
	}

	@Override
	public String toString() {
		return "OrderItem [id=" + id + ", product=" + product + ", count=" + count + "]";
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public OrderItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OrderItem(String product, int count) {
		super();
		this.product = product;
		this.count = count;
	}

	public MyOrder getOrder() {
		return order;
	}

	public void setOrder(MyOrder order) {
		this.order = order;
	}

	public Integer getId() {
		return id;
	}

}
