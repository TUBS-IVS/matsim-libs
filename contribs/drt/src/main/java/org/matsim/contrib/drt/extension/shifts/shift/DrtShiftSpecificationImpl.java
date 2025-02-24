package org.matsim.contrib.drt.extension.shifts.shift;

import org.matsim.api.core.v01.Id;

/**
 * @author nkuehnel / MOIA
 */
public class DrtShiftSpecificationImpl implements DrtShiftSpecification {

	private final Id<DrtShift> id;
	private final double start;
	private final double end;
	private final DrtShiftBreakSpecification shiftBreak;

	private DrtShiftSpecificationImpl(Builder builder) {
		this.id = builder.id;
		this.start = builder.start;
		this.end = builder.end;
		this.shiftBreak = builder.shiftBreak;
	}

	@Override
	public double getStartTime() {
		return start;
	}

	@Override
	public double getEndTime() {
		return end;
	}

	@Override
	public DrtShiftBreakSpecification getBreak() {
		return shiftBreak;
	}

	@Override
	public Id<DrtShift> getId() {
		return id;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(DrtShiftSpecificationImpl copy) {
		Builder builder = new Builder();
		builder.id = copy.getId();
		builder.start = copy.getStartTime();
		builder.end = copy.getEndTime();
		builder.shiftBreak = copy.getBreak();
		return builder;
	}

	public static final class Builder {
		private Id<DrtShift> id;
		private double start;
		private double end;
		private DrtShiftBreakSpecification shiftBreak;

		private Builder() {
		}

		public Builder id(Id<DrtShift> val) {
			id = val;
			return this;
		}

		public Builder start(double val) {
			start = val;
			return this;
		}

		public Builder end(double val) {
			end = val;
			return this;
		}

		public Builder shiftBreak(DrtShiftBreakSpecification val) {
			shiftBreak = val;
			return this;
		}

		public DrtShiftSpecificationImpl build() {
			return new DrtShiftSpecificationImpl(this);
		}
	}
}
