package com.promineotech.jeep.entity;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@Builder
@AllArgsConstructor

public class Jeep {
	private Long modelPK;
		private JeepModel modelId;
	private String trimLevel;
	private int numDoors;
	private int wheelSize;
	private BigDecimal basePrice;
	
	
	//INSERT INTO models (model_id, trim_level, num_doors, wheel_size, base_price) VALUES('WRANGLER', 'Sport', 2, 17, 28475.00);
	//INSERT INTO models (model_id, trim_level, num_doors, wheel_size, base_price) VALUES('WRANGLER', 'Sport', 4, 17, 31975.00);

}
