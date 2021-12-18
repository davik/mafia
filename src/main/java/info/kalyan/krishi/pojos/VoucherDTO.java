package info.kalyan.krishi.pojos;

import info.kalyan.krishi.pojos.Voucher.VoucherType;

import org.joda.time.DateTime;

public class VoucherDTO {
	public DateTime transactionDate;
	public Voucher.VoucherType voucherType;
	public String transactionInfo = "";
	public double balance = 0;
	public double value = 0;
	public String particulars = "";
	public String productId = "";
	public String productName = "";
	public String manufacturer = "";
	public String unit = "";
	public String vendorName = "";
	public String warehouseName = "";
	public String warehouseId = "";
	public double quantity = 0;
	public double rate = 0;
	public String mode = "";
	public String voucherId = "";

	public VoucherDTO(Voucher voucher) {
		this.transactionDate = voucher.transactionDate;
		this.voucherType = voucher.voucherType;
		this.transactionInfo = voucher.transactionInfo;
		this.balance = voucher.balance;
		this.value = voucher.value;
		this.productId = voucher.productId;
		this.productName = voucher.productName;
		this.manufacturer = voucher.manufacturer;
		this.quantity = voucher.quantity;
		this.rate = voucher.rate;
		this.unit = voucher.unit;
		this.warehouseName = voucher.warehouseName;
		this.warehouseId = voucher.warehouseId;
		this.vendorName = voucher.vendorName;
		this.mode = voucher.mode;
		this.voucherId = voucher.voucherId;
		if (voucher.voucherType == VoucherType.PURCHASE || voucher.voucherType == VoucherType.SALE) {
			this.particulars = voucher.productName + "(" + voucher.manufacturer + ") " + voucher.quantity + " "
					+ voucher.unit + " @" + voucher.rate;
		} else if (voucher.voucherType == VoucherType.PAYMENT || voucher.voucherType == VoucherType.RECEIPT) {
			this.particulars = voucher.mode;
		}
	}
}