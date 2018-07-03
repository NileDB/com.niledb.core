DROP SCHEMA IF EXISTS "Models" CASCADE;

CREATE SCHEMA IF NOT EXISTS "Models";
SET search_path TO "Models";

CREATE TYPE "Models"."customTypeAttributeType" AS ENUM (
	'Text',
	'Boolean',
	'Integer',
	'Decimal',
	'Money',
	'Date',
	'Timestamp',
	'CustomType',
	'Bytea',
	'Smallint',
	'Bitint',
	'DoublePrecision',
	'Real',
	'Varchar',
	'Char',
	'Time',
	'Interval',
	'TimestampWithTimeZone',
	'TimeWithTimeZone',
	'Point'
);

CREATE TYPE "Models"."entityAttributeType" AS ENUM (
	'Text',
	'Boolean',
	'Integer',
	'Cecimal',
	'Money',
	'Date',
	'Timestamp',
	'CustomType',
	'Serial',
	'Bytea',
	'Smallint',
	'Bitint',
	'DoublePrecision',
	'Real',
	'Smallserial',
	'Bigserial',
	'Varchar',
	'Char',
	'Time',
	'Interval',
	'TimestampWithTimeZone',
	'TimeWithTimeZone',
	'Point'
);

CREATE TYPE "Models"."entityType" AS ENUM (
	'Table',
	'External'
);

CREATE TABLE "Models"."Schema" (
	"id" serial PRIMARY KEY,
	"name" text NOT NULL,
	"timestamp" timestamp DEFAULT now()
);

CREATE UNIQUE INDEX ON "Models"."Schema"("name");

CREATE TABLE "Models"."CustomType" (
	"id" serial PRIMARY KEY,
	"eContainer" int,
	"name" text NOT NULL,
	"documentation" text,
	"timestamp" timestamp DEFAULT now()
);

CREATE UNIQUE INDEX ON "Models"."CustomType"("eContainer", "name");

CREATE TABLE "Models"."CustomTypeAttribute" (
	"id" serial PRIMARY KEY,
	"eContainer" int,
	"name" text NOT NULL,
	"type" "Models"."customTypeAttributeType" NOT NULL,
	"array" boolean NOT NULL DEFAULT false,
	"customType" int,
	"enumType" int,
	"length" int,
	"precision" int,
	"scale" int,
	"documentation" text,
	"timestamp" timestamp DEFAULT now()
);

CREATE UNIQUE INDEX ON "Models"."CustomTypeAttribute"("eContainer", "name");

CREATE TABLE "Models"."Entity" (
	"id" serial PRIMARY KEY,
	"eContainer" int,
	"name" text NOT NULL,
	"type" "Models"."entityType" NOT NULL,
	"documentation" text,
	"timestamp" timestamp DEFAULT now()
);

CREATE UNIQUE INDEX ON "Models"."Entity"("eContainer", "name");

CREATE TABLE "Models"."EntityAttribute" (
	"id" serial PRIMARY KEY,
	"eContainer" int,
	"name" text NOT NULL,
	"type" "Models"."entityAttributeType" NOT NULL,
	"array" boolean NOT NULL DEFAULT false,
	"defaultValue" text,
	"required" boolean NOT NULL DEFAULT false,
	"customType" int,
	"enumType" int,
	"length" int,
	"precision" int,
	"scale" int,
	"documentation" text,
	"timestamp" timestamp DEFAULT now()
);

CREATE UNIQUE INDEX ON "Models"."EntityAttribute"("eContainer", "name");

CREATE TABLE "Models"."EntityKey" (
	"id" serial PRIMARY KEY,
	"eContainer" int,
	"name" text NOT NULL,
	"unique" boolean NOT NULL DEFAULT false,
	"primaryKey" boolean NOT NULL DEFAULT false,
	"documentation" text,
	"timestamp" timestamp DEFAULT now()
);

CREATE UNIQUE INDEX ON "Models"."EntityKey"("eContainer", "name");

CREATE TABLE "Models"."EntityKeyEntityAttribute" (
	"id" serial PRIMARY KEY,
	"eContainer" int,
	"entityAttribute" int,
	"timestamp" timestamp DEFAULT now()
);

CREATE UNIQUE INDEX ON "Models"."EntityKeyEntityAttribute"("eContainer", "entityAttribute");

CREATE TABLE "Models"."EntityReference" (
	"id" serial PRIMARY KEY,
	"eContainer" int,
	"name" text NOT NULL,
	"referencedKey" int NOT NULL,
	"documentation" text,
	"timestamp" timestamp DEFAULT now()
);

CREATE UNIQUE INDEX ON "Models"."EntityReference"("eContainer", "name");

CREATE TABLE "Models"."EntityReferenceEntityAttribute" (
	"id" serial PRIMARY KEY,
	"eContainer" int,
	"entityAttribute" int,
	"timestamp" timestamp DEFAULT now()
);

CREATE UNIQUE INDEX ON "Models"."EntityReferenceEntityAttribute"("eContainer", "entityAttribute");

CREATE TABLE "Models"."EnumType" (
	"id" serial PRIMARY KEY,
	"eContainer" int,
	"name" text NOT NULL,
	"values" text[] NOT NULL,
	"documentation" text,
	"timestamp" timestamp DEFAULT now()
);

CREATE UNIQUE INDEX ON "Models"."EnumType"("eContainer", "name");

ALTER TABLE "Models"."CustomType" ADD CONSTRAINT "eContainer" FOREIGN KEY ("eContainer") REFERENCES "Models"."Schema"("id");
ALTER TABLE "Models"."CustomTypeAttribute" ADD CONSTRAINT "eContainer" FOREIGN KEY ("eContainer") REFERENCES "Models"."CustomType"("id");
ALTER TABLE "Models"."Entity" ADD CONSTRAINT "eContainer" FOREIGN KEY ("eContainer") REFERENCES "Models"."Schema"("id");
ALTER TABLE "Models"."EntityAttribute" ADD CONSTRAINT "eContainer" FOREIGN KEY ("eContainer") REFERENCES "Models"."Entity"("id");
ALTER TABLE "Models"."EntityKey" ADD CONSTRAINT "eContainer" FOREIGN KEY ("eContainer") REFERENCES "Models"."Entity"("id");
ALTER TABLE "Models"."EntityKeyEntityAttribute" ADD CONSTRAINT "eContainer" FOREIGN KEY ("eContainer") REFERENCES "Models"."EntityKey"("id");
ALTER TABLE "Models"."EntityReference" ADD CONSTRAINT "eContainer" FOREIGN KEY ("eContainer") REFERENCES "Models"."Entity"("id");
ALTER TABLE "Models"."EntityReferenceEntityAttribute" ADD CONSTRAINT "eContainer" FOREIGN KEY ("eContainer") REFERENCES "Models"."EntityReference"("id");
ALTER TABLE "Models"."EnumType" ADD CONSTRAINT "eContainer" FOREIGN KEY ("eContainer") REFERENCES "Models"."Schema"("id");
ALTER TABLE "Models"."CustomTypeAttribute" ADD CONSTRAINT "customType" FOREIGN KEY ("customType") REFERENCES "Models"."CustomType"("id");
ALTER TABLE "Models"."CustomTypeAttribute" ADD CONSTRAINT "enumType" FOREIGN KEY ("enumType") REFERENCES "Models"."EnumType"("id");
ALTER TABLE "Models"."EntityAttribute" ADD CONSTRAINT "customType" FOREIGN KEY ("customType") REFERENCES "Models"."CustomType"("id");
ALTER TABLE "Models"."EntityAttribute" ADD CONSTRAINT "enumType" FOREIGN KEY ("enumType") REFERENCES "Models"."EnumType"("id");
ALTER TABLE "Models"."EntityKeyEntityAttribute" ADD CONSTRAINT "entityAttribute" FOREIGN KEY ("entityAttribute") REFERENCES "Models"."EntityAttribute"("id");
ALTER TABLE "Models"."EntityReference" ADD CONSTRAINT "referencedKey" FOREIGN KEY ("referencedKey") REFERENCES "Models"."EntityKey"("id");
ALTER TABLE "Models"."EntityReferenceEntityAttribute" ADD CONSTRAINT "entityAttribute" FOREIGN KEY ("entityAttribute") REFERENCES "Models"."EntityAttribute"("id");

CREATE OR REPLACE FUNCTION "Models"."createSchema"()
	RETURNS TRIGGER AS $createSchema$
	DECLARE
		selectedSchema RECORD;
	BEGIN
		EXECUTE 'CREATE SCHEMA IF NOT EXISTS ' || NEW."name";
		RETURN null;
	END;
	$createSchema$ language 'plpgsql';

CREATE TRIGGER "createSchema" 
	AFTER INSERT ON "Models"."Schema" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."createSchema"();

CREATE OR REPLACE FUNCTION "Models"."deleteSchema"()
	RETURNS TRIGGER AS $deleteSchema$
	BEGIN
		EXECUTE 'DROP SCHEMA IF EXISTS ' || OLD."name" || ' CASCADE';
		RETURN null;
	END;
	$deleteSchema$ language 'plpgsql';

CREATE TRIGGER "deleteSchema" 
	AFTER DELETE ON "Models"."Schema" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."deleteSchema"();

CREATE OR REPLACE FUNCTION "Models"."updateSchema"()
	RETURNS TRIGGER AS $updateSchema$
	DECLARE
		selectedSchema RECORD;
	BEGIN
		EXECUTE 'ALTER SCHEMA ' || OLD."name" || ' RENAME TO ' || NEW."name";
		RETURN null;
	END;
	$updateSchema$ language 'plpgsql';

CREATE TRIGGER "updateSchema" 
	AFTER UPDATE ON "Models"."Schema" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."updateSchema"();
