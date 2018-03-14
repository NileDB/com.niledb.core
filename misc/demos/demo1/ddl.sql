DROP TABLE IF EXISTS "orderLine";
DROP TABLE IF EXISTS "order";
DROP TABLE IF EXISTS "product";
DROP TABLE IF EXISTS "courier";
DROP TABLE IF EXISTS "address";
DROP TABLE IF EXISTS "customer";

DROP TYPE IF EXISTS "orderDeliveryStatus";

CREATE TYPE "orderDeliveryStatus" AS ENUM (
	'PENDING',
	'AWAITING PAYMENT',
	'AWAITING FULFILLMENT',
	'AWAITING SHIPMENT',
	'AWAITING PICKUP',
	'PARTIALLY SHIPPED',
	'COMPLETED',
	'SHIPPED',
	'CANCELLED',
	'DECLINED',
	'REFUNDED',
	'DISPUTED',
	'VERIFICATION REQUIRED',
	'PARTIALLY REFUNDED'
);

CREATE TABLE "address" (
	"id" serial PRIMARY KEY,
	"customer" int NOT NULL,
	"fullName" text,
	"phoneNumber" text,
	"addressLine1" text NOT NULL,
	"addressLine2" text,
	"postalCode" text,
	"city" text NOT NULL,
	"province" text NOT NULL,
	"comments" text
);

CREATE TABLE "courier" (
	"id" serial PRIMARY KEY,
	"user" text NOT NULL,
	"fullName" text NOT NULL,
	"latitude" double precision,
	"longitude" double precision
);

CREATE TABLE "customer" (
	"id" serial PRIMARY KEY,
	"user" text NOT NULL,
	"fullName" text NOT NULL,
	"email" text NOT NULL
);

CREATE TABLE "order" (
	"id" serial PRIMARY KEY,
	"customer" int NOT NULL,
	"orderNumber" text NOT NULL,
	"total" double precision,
	"orderDate" date NOT NULL,
	"deliveryDate" date,
	"shippingAddress" int,
	"deliveryStatus" "orderDeliveryStatus" NOT NULL DEFAULT 'PENDING',
	"courier" int,
	"trackingId" text
);

CREATE TABLE "orderLine" (
	"id" serial PRIMARY KEY,
	"order" int NOT NULL,
	"product" int NOT NULL,
	"quantity" int NOT NULL DEFAULT 1,
	"price" double precision NOT NULL DEFAULT 0,
	"gtin" text,
	"name" text
);

CREATE TABLE "product" (
	"id" serial PRIMARY KEY,
	"gtin" text,
	"name" text NOT NULL,
	"image" bytea
);

ALTER TABLE "orderLine" ADD CONSTRAINT "product" FOREIGN KEY ("product") REFERENCES "product"("id");
ALTER TABLE "orderLine" ADD CONSTRAINT "order" FOREIGN KEY ("order") REFERENCES "order"("id");
ALTER TABLE "order" ADD CONSTRAINT "courier" FOREIGN KEY ("courier") REFERENCES "courier"("id");
ALTER TABLE "order" ADD CONSTRAINT "customer" FOREIGN KEY ("customer") REFERENCES "customer"("id");
ALTER TABLE "order" ADD CONSTRAINT "shippingAddress" FOREIGN KEY ("shippingAddress") REFERENCES "address"("id");
ALTER TABLE "address" ADD CONSTRAINT "customer" FOREIGN KEY ("customer") REFERENCES "customer"("id");

CREATE UNIQUE INDEX "user" ON "courier"("user");
CREATE UNIQUE INDEX "user" ON "customer"("user");
CREATE UNIQUE INDEX "email" ON "customer"("email");
CREATE UNIQUE INDEX "name" ON "product"("name");
CREATE UNIQUE INDEX "orderNumber" ON "order"("orderNumber");
