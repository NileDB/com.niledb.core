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
	'Decimal',
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

-- SCHEMAS

CREATE OR REPLACE FUNCTION "Models"."createSchema"()
	RETURNS TRIGGER AS $createSchema$
	BEGIN
		EXECUTE 'CREATE SCHEMA IF NOT EXISTS "' || NEW."name" || '"';
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
		EXECUTE 'DROP SCHEMA IF EXISTS "' || OLD."name" || '" CASCADE';
		RETURN null;
	END;
	$deleteSchema$ language 'plpgsql';

CREATE TRIGGER "deleteSchema" 
	AFTER DELETE ON "Models"."Schema" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."deleteSchema"();

CREATE OR REPLACE FUNCTION "Models"."updateSchema"()
	RETURNS TRIGGER AS $updateSchema$
	BEGIN
		EXECUTE 'ALTER SCHEMA "' || OLD."name" || '" RENAME TO "' || NEW."name" || '"';
		RETURN null;
	END;
	$updateSchema$ language 'plpgsql';

CREATE TRIGGER "updateSchema" 
	AFTER UPDATE ON "Models"."Schema" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."updateSchema"();

-- ENUM TYPES

CREATE OR REPLACE FUNCTION "Models"."createEnumType"()
	RETURNS TRIGGER AS $createEnumType$
	DECLARE
		selectedSchema RECORD;
		sql text;
		value text;
		i int;
	BEGIN
		FOR selectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = NEW."eContainer" LOOP
			sql := 'CREATE TYPE "' || selectedSchema."name" || '"."' || NEW."name" || '" AS ENUM (';

			i := 0;
			FOREACH value IN ARRAY NEW."values" LOOP
				IF NOT i = 0 THEN
					sql := sql || ', ';
				END IF;
				sql := sql || '''' || value || '''';
				i := i + 1;
			END LOOP;

			sql := sql || ')';
			EXECUTE sql;
		END LOOP;
		RETURN null;
	END;
	$createEnumType$ language 'plpgsql';

CREATE TRIGGER "createEnumType" 
	AFTER INSERT ON "Models"."EnumType" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."createEnumType"();

CREATE OR REPLACE FUNCTION "Models"."deleteEnumType"()
	RETURNS TRIGGER AS $deleteEnumType$
	DECLARE
		selectedSchema RECORD;
	BEGIN
		FOR selectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = OLD."eContainer" LOOP
			EXECUTE 'DROP TYPE "' || selectedSchema."name" || '"."' || OLD."name" || '"';
		END LOOP;
		RETURN null;
	END;
	$deleteEnumType$ language 'plpgsql';

CREATE TRIGGER "deleteEnumType" 
	AFTER DELETE ON "Models"."EnumType" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."deleteEnumType"();

CREATE OR REPLACE FUNCTION "Models"."updateEnumType"()
	RETURNS TRIGGER AS $updateEnumType$
	DECLARE
		selectedSchema RECORD;
		sql text;
		value text;
		newValue text;
		oldValue text;
		i int;
		isMissing boolean;
		isLeft boolean;
	BEGIN
		FOR selectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = OLD."eContainer" LOOP
			IF NOT OLD."eContainer" = NEW."eContainer" THEN
				EXECUTE 'DROP TYPE "' || selectedSchema."name" || '"."' || OLD."name" || '"';
				
				sql := 'CREATE TYPE "' || selectedSchema."name" || '"."' || NEW."name" || '" AS ENUM (';
	
				i := 0;
				FOREACH value IN ARRAY NEW."values" LOOP
					IF NOT i = 0 THEN
						sql := sql || ', ';
					END IF;
					sql := sql || '''' || value || '''';
					i := i + 1;
				END LOOP;
	
				sql := sql || ')';
				EXECUTE sql;
			ELSE 
				IF NOT OLD."name" = NEW."name" THEN
					EXECUTE 'ALTER TYPE "' || selectedSchema."name" || '"."' || OLD."name" || '" RENAME TO "' || NEW."name" || '"';
				END IF;

				-- ALTER TYPE ... ADD cannot be executed from a function or multi-command (pues vaya caca)

				FOREACH newValue IN ARRAY NEW."values" LOOP
					isMissing := true;
					FOREACH oldValue IN ARRAY OLD."values" LOOP
						IF oldValue = newValue THEN
							isMissing := false;
							EXIT;
						END IF;
					END LOOP;
					IF isMissing THEN
						EXIT;
					END IF;
				END LOOP;
				
				FOREACH oldValue IN ARRAY OLD."values" LOOP
					isLeft := true;
					FOREACH newValue IN ARRAY NEW."values" LOOP
						IF oldValue = newValue THEN
							isLeft := false;
							EXIT;
						END IF;
					END LOOP;
					IF isLeft THEN
						EXIT;
					END IF;
				END LOOP;
				
				IF isMissing OR isLeft THEN
					EXECUTE 'DROP TYPE "' || selectedSchema."name" || '"."' || OLD."name" || '"';
					
					sql := 'CREATE TYPE "' || selectedSchema."name" || '"."' || NEW."name" || '" AS ENUM (';
		
					i := 0;
					FOREACH value IN ARRAY NEW."values" LOOP
						IF NOT i = 0 THEN
							sql := sql || ', ';
						END IF;
						sql := sql || '''' || value || '''';
						i := i + 1;
					END LOOP;
		
					sql := sql || ')';
					EXECUTE sql;
				END IF;				
			END IF;
		END LOOP;
		RETURN null;
	END;
	$updateEnumType$ language 'plpgsql';

CREATE TRIGGER "updateEnumType" 
	AFTER UPDATE ON "Models"."EnumType" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."updateEnumType"();

-- CUSTOM TYPES

CREATE OR REPLACE FUNCTION "Models"."createCustomType"()
	RETURNS TRIGGER AS $createCustomType$
	DECLARE
		selectedSchema RECORD;
		sql text;
		i int;
	BEGIN
		FOR selectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = NEW."eContainer" LOOP
			sql := 'CREATE TYPE "' || selectedSchema."name" || '"."' || NEW."name" || '" AS (_reserved boolean)';
			EXECUTE sql;
		END LOOP;
		RETURN null;
	END;
	$createCustomType$ language 'plpgsql';

CREATE TRIGGER "createCustomType" 
	AFTER INSERT ON "Models"."CustomType" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."createCustomType"();

CREATE OR REPLACE FUNCTION "Models"."deleteCustomType"()
	RETURNS TRIGGER AS $deleteCustomType$
	DECLARE
		selectedSchema RECORD;
	BEGIN
		FOR selectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = OLD."eContainer" LOOP
			EXECUTE 'DROP TYPE "' || selectedSchema."name" || '"."' || OLD."name" || '"';
		END LOOP;
		RETURN null;
	END;
	$deleteCustomType$ language 'plpgsql';

CREATE TRIGGER "deleteCustomType" 
	AFTER DELETE ON "Models"."CustomType" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."deleteCustomType"();

CREATE OR REPLACE FUNCTION "Models"."updateCustomType"()
	RETURNS TRIGGER AS $updateCustomType$
	DECLARE
		selectedSchema RECORD;
		newSelectedSchema RECORD;
		sql text;
	BEGIN
		FOR selectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = OLD."eContainer" LOOP
			IF NOT OLD."eContainer" = NEW."eContainer" THEN
				EXECUTE 'DROP TYPE "' || selectedSchema."name" || '"."' || OLD."name" || '"';
				
				FOR newSelectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = NEW."eContainer" LOOP
					sql := 'CREATE TYPE "' || newSelectedSchema."name" || '"."' || NEW."name" || '" AS (_reserved boolean)';
					EXECUTE sql;
				END LOOP;
			ELSE 
				IF NOT OLD."name" = NEW."name" THEN
					EXECUTE 'ALTER TYPE "' || selectedSchema."name" || '"."' || OLD."name" || '" RENAME TO "' || NEW."name" || '"';
				END IF;
			END IF;
		END LOOP;
		RETURN null;
	END;
	$updateCustomType$ language 'plpgsql';

CREATE TRIGGER "updateCustomType" 
	AFTER UPDATE ON "Models"."CustomType" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."updateCustomType"();

-- TABLES

CREATE OR REPLACE FUNCTION "Models"."createEntity"()
	RETURNS TRIGGER AS $createEntity$
	DECLARE
		selectedSchema RECORD;
		sql text;
		i int;
	BEGIN
		FOR selectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = NEW."eContainer" LOOP
			sql := 'CREATE TABLE "' || selectedSchema."name" || '"."' || NEW."name" || '" (_reserved boolean)';
			EXECUTE sql;
		END LOOP;
		RETURN null;
	END;
	$createEntity$ language 'plpgsql';

CREATE TRIGGER "createEntity" 
	AFTER INSERT ON "Models"."Entity" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."createEntity"();

CREATE OR REPLACE FUNCTION "Models"."deleteEntity"()
	RETURNS TRIGGER AS $deleteEntity$
	DECLARE
		selectedSchema RECORD;
	BEGIN
		FOR selectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = OLD."eContainer" LOOP
			EXECUTE 'DROP TABLE "' || selectedSchema."name" || '"."' || OLD."name" || '"';
		END LOOP;
		RETURN null;
	END;
	$deleteEntity$ language 'plpgsql';

CREATE TRIGGER "deleteEntity" 
	AFTER DELETE ON "Models"."Entity" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."deleteEntity"();

CREATE OR REPLACE FUNCTION "Models"."updateEntity"()
	RETURNS TRIGGER AS $updateEntity$
	DECLARE
		selectedSchema RECORD;
		newSelectedSchema RECORD;
		sql text;
	BEGIN
		FOR selectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = OLD."eContainer" LOOP
			IF NOT OLD."eContainer" = NEW."eContainer" THEN
				EXECUTE 'DROP TABLE "' || selectedSchema."name" || '"."' || OLD."name" || '"';
				
				FOR newSelectedSchema IN SELECT "id", "name" FROM "Models"."Schema" WHERE "id" = NEW."eContainer" LOOP
					sql := 'CREATE TABLE "' || newSelectedSchema."name" || '"."' || NEW."name" || '" (_reserved boolean)';
					EXECUTE sql;
				END LOOP;
			ELSE 
				IF NOT OLD."name" = NEW."name" THEN
					EXECUTE 'ALTER TABLE "' || selectedSchema."name" || '"."' || OLD."name" || '" RENAME TO "' || NEW."name" || '"';
				END IF;
			END IF;
		END LOOP;
		RETURN null;
	END;
	$updateEntity$ language 'plpgsql';

CREATE TRIGGER "updateEntity" 
	AFTER UPDATE ON "Models"."Entity" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."updateEntity"();

-- CUSTOM TYPE ATTRIBUTES

CREATE OR REPLACE FUNCTION "Models"."createCustomTypeAttribute"()
	RETURNS TRIGGER AS $createCustomTypeAttribute$
	DECLARE
		selectedCustomType RECORD;
		sql text;
		customType RECORD;
		enumType RECORD;
	BEGIN
		FOR selectedCustomType IN SELECT c."id", c."name", s."name" AS "schema" FROM "Models"."CustomType" c, "Models"."Schema" s WHERE c."id" = NEW."eContainer" AND c."eContainer" = s."id" LOOP
			sql := 'ALTER TYPE "' || selectedCustomType."schema" || '"."' || selectedCustomType."name" || '" ADD ATTRIBUTE "' || NEW."name" || '" ';
			CASE NEW."type"
				WHEN 'Text' THEN
					sql := sql || 'text';
				WHEN 'Boolean' THEN
					sql := sql || 'boolean';
				WHEN 'Integer' THEN
					sql := sql || 'int';
				WHEN 'Decimal' THEN
					sql := sql || 'decimal';
				WHEN 'Money' THEN
					sql := sql || 'money';
				WHEN 'Date' THEN
					sql := sql || 'date';
				WHEN 'Timestamp' THEN
					sql := sql || 'timestamp';
				WHEN 'CustomType' THEN
					IF NEW."customType" IS NOT NULL THEN
						FOR customType IN SELECT "id", "name" FROM "Models"."CustomType" WHERE "id" = NEW."customType" LOOP
							sql := sql || '"' || customType."name" || '"';
						END LOOP;
					ELSIF NEW."enumType" IS NOT NULL THEN
						FOR enumType IN SELECT "id", "name" FROM "Models"."EnumType" WHERE "id" = NEW."enumType" LOOP
							sql := sql || '"' || enumType."name" || '"';
						END LOOP;
					ELSE
						sql := sql || 'text';
					END IF;
				WHEN 'Bytea' THEN
					sql := sql || 'bytea';
				WHEN 'Smallint' THEN
					sql := sql || 'smallint';
				WHEN 'Bitint' THEN
					sql := sql || 'bitint';
				WHEN 'DoublePrecision' THEN
					sql := sql || 'double precision';
				WHEN 'Real' THEN
					sql := sql || 'real';
				WHEN 'Varchar' THEN
					sql := sql || 'varchar';
				WHEN 'Char' THEN
					sql := sql || 'char';
				WHEN 'Time' THEN
					sql := sql || 'time';
				WHEN 'Interval' THEN
					sql := sql || 'interval';
				WHEN 'TimestampWithTimeZone' THEN
					sql := sql || 'timestamp with time zone';
				WHEN 'TimeWithTimeZone' THEN
					sql := sql || 'time with time zone';
				WHEN 'Point' THEN
					sql := sql || 'point';
				ELSE
					sql := sql || 'text';
			END CASE;
			IF NEW.length IS NOT NULL THEN
				sql := sql || '(' || NEW.length || ')';
			ELSIF NEW.precision IS NOT NULL THEN
				IF NEW.scale IS NOT NULL THEN
					sql := sql || '(' || NEW.precision || ',' || NEW.scale || ')';
				ELSE
					sql := sql || '(' || NEW.precision || ')';
				END IF;
			END IF;
			IF NEW.array THEN
				sql := sql || '[]';
			END IF;
			EXECUTE sql;
		END LOOP;
		RETURN null;
	END;
	$createCustomTypeAttribute$ language 'plpgsql';

CREATE TRIGGER "createCustomTypeAttribute" 
	AFTER INSERT ON "Models"."CustomTypeAttribute" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."createCustomTypeAttribute"();

CREATE OR REPLACE FUNCTION "Models"."deleteCustomTypeAttribute"()
	RETURNS TRIGGER AS $deleteCustomTypeAttribute$
	DECLARE
		selectedCustomType RECORD;
	BEGIN
		FOR selectedCustomType IN SELECT c."id", c."name", s."name" AS "schema" FROM "Models"."CustomType" c, "Models"."Schema" s WHERE c."id" = OLD."eContainer" AND c."eContainer" = s."id" LOOP
			EXECUTE 'ALTER TYPE "' || selectedCustomType."schema" || '"."' || selectedCustomType."name" || '" DROP ATTRIBUTE "' || OLD."name" || '"';
		END LOOP;
		RETURN null;
	END;
	$deleteCustomTypeAttribute$ language 'plpgsql';

CREATE TRIGGER "deleteCustomTypeAttribute" 
	AFTER DELETE ON "Models"."CustomTypeAttribute" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."deleteCustomTypeAttribute"();

CREATE OR REPLACE FUNCTION "Models"."updateCustomTypeAttribute"()
	RETURNS TRIGGER AS $updateCustomTypeAttribute$
	DECLARE
		selectedCustomType RECORD;
		sql text;
		customType RECORD;
		enumType RECORD;
	BEGIN
		FOR selectedCustomType IN SELECT c."id", c."name", s."name" AS "schema" FROM "Models"."CustomType" c, "Models"."Schema" s WHERE c."id" = OLD."eContainer" AND c."eContainer" = s."id" LOOP
			EXECUTE 'ALTER TYPE "' || selectedCustomType."schema" || '"."' || selectedCustomType."name" || '" DROP ATTRIBUTE "' || OLD."name" || '"';
		END LOOP;
		
		FOR selectedCustomType IN SELECT c."id", c."name", s."name" AS "schema" FROM "Models"."CustomType" c, "Models"."Schema" s WHERE c."id" = NEW."eContainer" AND c."eContainer" = s."id" LOOP
			sql := 'ALTER TYPE "' || selectedCustomType."schema" || '"."' || selectedCustomType."name" || '" ADD ATTRIBUTE "' || NEW."name" || '" ';
			CASE NEW."type"
				WHEN 'Text' THEN
					sql := sql || 'text';
				WHEN 'Boolean' THEN
					sql := sql || 'boolean';
				WHEN 'Integer' THEN
					sql := sql || 'int';
				WHEN 'Decimal' THEN
					sql := sql || 'decimal';
				WHEN 'Money' THEN
					sql := sql || 'money';
				WHEN 'Date' THEN
					sql := sql || 'date';
				WHEN 'Timestamp' THEN
					sql := sql || 'timestamp';
				WHEN 'CustomType' THEN
					IF NEW."customType" IS NOT NULL THEN
						FOR customType IN SELECT "id", "name" FROM "Models"."CustomType" WHERE "id" = NEW."customType" LOOP
							sql := sql || '"' || customType."name" || '"';
						END LOOP;
					ELSIF NEW."enumType" IS NOT NULL THEN
						FOR enumType IN SELECT "id", "name" FROM "Models"."EnumType" WHERE "id" = NEW."enumType" LOOP
							sql := sql || '"' || enumType."name" || '"';
						END LOOP;
					ELSE
						sql := sql || 'text';
					END IF;
				WHEN 'Bytea' THEN
					sql := sql || 'bytea';
				WHEN 'Smallint' THEN
					sql := sql || 'smallint';
				WHEN 'Bitint' THEN
					sql := sql || 'bitint';
				WHEN 'DoublePrecision' THEN
					sql := sql || 'double precision';
				WHEN 'Real' THEN
					sql := sql || 'real';
				WHEN 'Varchar' THEN
					sql := sql || 'varchar';
				WHEN 'Char' THEN
					sql := sql || 'char';
				WHEN 'Time' THEN
					sql := sql || 'time';
				WHEN 'Interval' THEN
					sql := sql || 'interval';
				WHEN 'TimestampWithTimeZone' THEN
					sql := sql || 'timestamp with time zone';
				WHEN 'TimeWithTimeZone' THEN
					sql := sql || 'time with time zone';
				WHEN 'Point' THEN
					sql := sql || 'point';
				ELSE
					sql := sql || 'text';
			END CASE;
			IF NEW.length IS NOT NULL THEN
				sql := sql || '(' || NEW.length || ')';
			ELSIF NEW.precision IS NOT NULL THEN
				IF NEW.scale IS NOT NULL THEN
					sql := sql || '(' || NEW.precision || ',' || NEW.scale || ')';
				ELSE
					sql := sql || '(' || NEW.precision || ')';
				END IF;
			END IF;
			IF NEW.array THEN
				sql := sql || '[]';
			END IF;
			EXECUTE sql;
		END LOOP;
		RETURN null;
	END;
	$updateCustomTypeAttribute$ language 'plpgsql';

CREATE TRIGGER "updateCustomTypeAttribute" 
	AFTER UPDATE ON "Models"."CustomTypeAttribute" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."updateCustomTypeAttribute"();

-- ENTITY ATTRIBUTES

CREATE OR REPLACE FUNCTION "Models"."createEntityAttribute"()
	RETURNS TRIGGER AS $createEntityAttribute$
	DECLARE
		selectedEntity RECORD;
		sql text;
		customType RECORD;
		enumType RECORD;
	BEGIN
		FOR selectedEntity IN SELECT c."id", c."name", s."name" AS "schema" FROM "Models"."Entity" c, "Models"."Schema" s WHERE c."id" = NEW."eContainer" AND c."eContainer" = s."id" LOOP
			sql := 'ALTER TABLE "' || selectedEntity."schema" || '"."' || selectedEntity."name" || '" ADD COLUMN "' || NEW."name" || '" ';
			CASE NEW."type"
				WHEN 'Text' THEN
					sql := sql || 'text';
				WHEN 'Boolean' THEN
					sql := sql || 'boolean';
				WHEN 'Integer' THEN
					sql := sql || 'int';
				WHEN 'Decimal' THEN
					sql := sql || 'decimal';
				WHEN 'Money' THEN
					sql := sql || 'money';
				WHEN 'Date' THEN
					sql := sql || 'date';
				WHEN 'Timestamp' THEN
					sql := sql || 'timestamp';
				WHEN 'CustomType' THEN
					IF NEW."customType" IS NOT NULL THEN
						FOR customType IN SELECT "id", "name" FROM "Models"."CustomType" WHERE "id" = NEW."customType" LOOP
							sql := sql || '"' || customType."name" || '"';
						END LOOP;
					ELSIF NEW."enumType" IS NOT NULL THEN
						FOR enumType IN SELECT "id", "name" FROM "Models"."EnumType" WHERE "id" = NEW."enumType" LOOP
							sql := sql || '"' || enumType."name" || '"';
						END LOOP;
					ELSE
						sql := sql || 'text';
					END IF;
				WHEN 'Serial' THEN
					sql := sql || 'serial';
				WHEN 'Bytea' THEN
					sql := sql || 'bytea';
				WHEN 'Smallint' THEN
					sql := sql || 'smallint';
				WHEN 'Bitint' THEN
					sql := sql || 'bitint';
				WHEN 'DoublePrecision' THEN
					sql := sql || 'double precision';
				WHEN 'Real' THEN
					sql := sql || 'real';
				WHEN 'Smallserial' THEN
					sql := sql || 'smallserial';
				WHEN 'Bigserial' THEN
					sql := sql || 'bigserial';
				WHEN 'Varchar' THEN
					sql := sql || 'varchar';
				WHEN 'Char' THEN
					sql := sql || 'char';
				WHEN 'Time' THEN
					sql := sql || 'time';
				WHEN 'Interval' THEN
					sql := sql || 'interval';
				WHEN 'TimestampWithTimeZone' THEN
					sql := sql || 'timestamp with time zone';
				WHEN 'TimeWithTimeZone' THEN
					sql := sql || 'time with time zone';
				WHEN 'Point' THEN
					sql := sql || 'point';
				ELSE
					sql := sql || 'text';
			END CASE;
			IF NEW.length IS NOT NULL THEN
				sql := sql || '(' || NEW.length || ')';
			ELSIF NEW.precision IS NOT NULL THEN
				IF NEW.scale IS NOT NULL THEN
					sql := sql || '(' || NEW.precision || ',' || NEW.scale || ')';
				ELSE
					sql := sql || '(' || NEW.precision || ')';
				END IF;
			END IF;
			IF NEW.array THEN
				sql := sql || '[]';
			END IF;
			IF NEW.required THEN
				sql := sql || ' NOT NULL';
			END IF;
			EXECUTE sql;
		END LOOP;
		RETURN null;
	END;
	$createEntityAttribute$ language 'plpgsql';

CREATE TRIGGER "createEntityAttribute" 
	AFTER INSERT ON "Models"."EntityAttribute" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."createEntityAttribute"();

CREATE OR REPLACE FUNCTION "Models"."deleteEntityAttribute"()
	RETURNS TRIGGER AS $deleteEntityAttribute$
	DECLARE
		selectedEntity RECORD;
	BEGIN
		FOR selectedEntity IN SELECT c."id", c."name", s."name" AS "schema" FROM "Models"."Entity" c, "Models"."Schema" s WHERE c."id" = OLD."eContainer" AND c."eContainer" = s."id" LOOP
			EXECUTE 'ALTER TABLE "' || selectedEntity."schema" || '"."' || selectedEntity."name" || '" DROP COLUMN "' || OLD."name" || '"';
		END LOOP;
		RETURN null;
	END;
	$deleteEntityAttribute$ language 'plpgsql';

CREATE TRIGGER "deleteEntityAttribute" 
	AFTER DELETE ON "Models"."EntityAttribute" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."deleteEntityAttribute"();

CREATE OR REPLACE FUNCTION "Models"."updateEntityAttribute"()
	RETURNS TRIGGER AS $updateEntityAttribute$
	DECLARE
		selectedEntity RECORD;
		sql text;
		customType RECORD;
		enumType RECORD;
	BEGIN
		FOR selectedEntity IN SELECT c."id", c."name", s."name" AS "schema" FROM "Models"."Entity" c, "Models"."Schema" s WHERE c."id" = OLD."eContainer" AND c."eContainer" = s."id" LOOP
			EXECUTE 'ALTER TABLE "' || selectedEntity."schema" || '"."' || selectedEntity."name" || '" DROP COLUMN "' || OLD."name" || '"';
		END LOOP;
		
		FOR selectedEntity IN SELECT c."id", c."name", s."name" AS "schema" FROM "Models"."Entity" c, "Models"."Schema" s WHERE c."id" = NEW."eContainer" AND c."eContainer" = s."id" LOOP
			sql := 'ALTER TABLE "' || selectedEntity."schema" || '"."' || selectedEntity."name" || '" ADD COLUMN "' || NEW."name" || '" ';
			CASE NEW."type"
				WHEN 'Text' THEN
					sql := sql || 'text';
				WHEN 'Boolean' THEN
					sql := sql || 'boolean';
				WHEN 'Integer' THEN
					sql := sql || 'int';
				WHEN 'Decimal' THEN
					sql := sql || 'decimal';
				WHEN 'Money' THEN
					sql := sql || 'money';
				WHEN 'Date' THEN
					sql := sql || 'date';
				WHEN 'Timestamp' THEN
					sql := sql || 'timestamp';
				WHEN 'CustomType' THEN
					IF NEW."customType" IS NOT NULL THEN
						FOR customType IN SELECT "id", "name" FROM "Models"."CustomType" WHERE "id" = NEW."customType" LOOP
							sql := sql || '"' || customType."name" || '"';
						END LOOP;
					ELSIF NEW."enumType" IS NOT NULL THEN
						FOR enumType IN SELECT "id", "name" FROM "Models"."EnumType" WHERE "id" = NEW."enumType" LOOP
							sql := sql || '"' || enumType."name" || '"';
						END LOOP;
					ELSE
						sql := sql || 'text';
					END IF;
				WHEN 'Serial' THEN
					sql := sql || 'serial';
				WHEN 'Bytea' THEN
					sql := sql || 'bytea';
				WHEN 'Smallint' THEN
					sql := sql || 'smallint';
				WHEN 'Bitint' THEN
					sql := sql || 'bitint';
				WHEN 'DoublePrecision' THEN
					sql := sql || 'double precision';
				WHEN 'Real' THEN
					sql := sql || 'real';
				WHEN 'Smallserial' THEN
					sql := sql || 'smallserial';
				WHEN 'Bigserial' THEN
					sql := sql || 'bigserial';
				WHEN 'Varchar' THEN
					sql := sql || 'varchar';
				WHEN 'Char' THEN
					sql := sql || 'char';
				WHEN 'Time' THEN
					sql := sql || 'time';
				WHEN 'Interval' THEN
					sql := sql || 'interval';
				WHEN 'TimestampWithTimeZone' THEN
					sql := sql || 'timestamp with time zone';
				WHEN 'TimeWithTimeZone' THEN
					sql := sql || 'time with time zone';
				WHEN 'Point' THEN
					sql := sql || 'point';
				ELSE
					sql := sql || 'text';
			END CASE;
			IF NEW.length IS NOT NULL THEN
				sql := sql || '(' || NEW.length || ')';
			ELSIF NEW.precision IS NOT NULL THEN
				IF NEW.scale IS NOT NULL THEN
					sql := sql || '(' || NEW.precision || ',' || NEW.scale || ')';
				ELSE
					sql := sql || '(' || NEW.precision || ')';
				END IF;
			END IF;
			IF NEW.array THEN
				sql := sql || '[]';
			END IF;
			IF NEW.required THEN
				sql := sql || ' NOT NULL';
			END IF;
			EXECUTE sql;
		END LOOP;
		RETURN null;
	END;
	$updateEntityAttribute$ language 'plpgsql';

CREATE TRIGGER "updateEntityAttribute" 
	AFTER UPDATE ON "Models"."EntityAttribute" 
	FOR EACH ROW 
	EXECUTE PROCEDURE "Models"."updateEntityAttribute"();
