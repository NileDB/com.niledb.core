/**
 * Copyright (C) 2018 NileDB, Inc.
 *
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License, version 3,
 *    as published by the Free Software Foundation.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package data;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Custom Type Attribute</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link data.CustomTypeAttribute#getName <em>Name</em>}</li>
 *   <li>{@link data.CustomTypeAttribute#getType <em>Type</em>}</li>
 *   <li>{@link data.CustomTypeAttribute#isArray <em>Array</em>}</li>
 *   <li>{@link data.CustomTypeAttribute#getCustomType <em>Custom Type</em>}</li>
 *   <li>{@link data.CustomTypeAttribute#getEnumType <em>Enum Type</em>}</li>
 *   <li>{@link data.CustomTypeAttribute#getLength <em>Length</em>}</li>
 *   <li>{@link data.CustomTypeAttribute#getPrecision <em>Precision</em>}</li>
 *   <li>{@link data.CustomTypeAttribute#getScale <em>Scale</em>}</li>
 *   <li>{@link data.CustomTypeAttribute#getDocumentation <em>Documentation</em>}</li>
 * </ul>
 *
 * @see data.DataPackage#getCustomTypeAttribute()
 * @model
 * @generated
 */
public interface CustomTypeAttribute {

	void eSetContainer(CustomType eContainer);
	CustomType eContainer();
	
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Name</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see data.DataPackage#getCustomTypeAttribute_Name()
	 * @model unique="false" required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link data.CustomTypeAttribute#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The default value is <code>"text"</code>.
	 * The literals are from the enumeration {@link data.CustomTypeAttributeType}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see data.CustomTypeAttributeType
	 * @see #setType(CustomTypeAttributeType)
	 * @see data.DataPackage#getCustomTypeAttribute_Type()
	 * @model default="text" unique="false" required="true"
	 * @generated
	 */
	CustomTypeAttributeType getType();

	/**
	 * Sets the value of the '{@link data.CustomTypeAttribute#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see data.CustomTypeAttributeType
	 * @see #getType()
	 * @generated
	 */
	void setType(CustomTypeAttributeType value);

	/**
	 * Returns the value of the '<em><b>Array</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Array</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Array</em>' attribute.
	 * @see #setArray(boolean)
	 * @see data.DataPackage#getCustomTypeAttribute_Array()
	 * @model default="false" unique="false" required="true"
	 * @generated
	 */
	boolean isArray();

	/**
	 * Sets the value of the '{@link data.CustomTypeAttribute#isArray <em>Array</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Array</em>' attribute.
	 * @see #isArray()
	 * @generated
	 */
	void setArray(boolean value);

	/**
	 * Returns the value of the '<em><b>Custom Type</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Custom Type</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Custom Type</em>' reference.
	 * @see #setCustomType(CustomType)
	 * @see data.DataPackage#getCustomTypeAttribute_CustomType()
	 * @model
	 * @generated
	 */
	CustomType getCustomType();

	/**
	 * Sets the value of the '{@link data.CustomTypeAttribute#getCustomType <em>Custom Type</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Custom Type</em>' reference.
	 * @see #getCustomType()
	 * @generated
	 */
	void setCustomType(CustomType value);

	/**
	 * Returns the value of the '<em><b>Enum Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Enum Type</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Enum Type</em>' attribute.
	 * @see #setEnumType(String)
	 * @see data.DataPackage#getCustomTypeAttribute_EnumType()
	 * @model
	 * @generated
	 */
	String getEnumType();

	/**
	 * Sets the value of the '{@link data.CustomTypeAttribute#getEnumType <em>Enum Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Enum Type</em>' attribute.
	 * @see #getEnumType()
	 * @generated
	 */
	void setEnumType(String value);

	/**
	 * Returns the value of the '<em><b>Length</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Length</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Length</em>' attribute.
	 * @see #setLength(int)
	 * @see data.DataPackage#getCustomTypeAttribute_Length()
	 * @model
	 * @generated
	 */
	int getLength();

	/**
	 * Sets the value of the '{@link data.CustomTypeAttribute#getLength <em>Length</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Length</em>' attribute.
	 * @see #getLength()
	 * @generated
	 */
	void setLength(int value);

	/**
	 * Returns the value of the '<em><b>Precision</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Precision</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Precision</em>' attribute.
	 * @see #setPrecision(int)
	 * @see data.DataPackage#getCustomTypeAttribute_Precision()
	 * @model
	 * @generated
	 */
	int getPrecision();

	/**
	 * Sets the value of the '{@link data.CustomTypeAttribute#getPrecision <em>Precision</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Precision</em>' attribute.
	 * @see #getPrecision()
	 * @generated
	 */
	void setPrecision(int value);

	/**
	 * Returns the value of the '<em><b>Scale</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Scale</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Scale</em>' attribute.
	 * @see #setScale(int)
	 * @see data.DataPackage#getCustomTypeAttribute_Scale()
	 * @model
	 * @generated
	 */
	int getScale();

	/**
	 * Sets the value of the '{@link data.CustomTypeAttribute#getScale <em>Scale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scale</em>' attribute.
	 * @see #getScale()
	 * @generated
	 */
	void setScale(int value);

	/**
	 * Returns the value of the '<em><b>Documentation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Documentation</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Documentation</em>' attribute.
	 * @see #setDocumentation(String)
	 * @see data.DataPackage#getCustomTypeAttribute_Documentation()
	 * @model
	 * @generated
	 */
	String getDocumentation();

	/**
	 * Sets the value of the '{@link data.CustomTypeAttribute#getDocumentation <em>Documentation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Documentation</em>' attribute.
	 * @see #getDocumentation()
	 * @generated
	 */
	void setDocumentation(String value);

} // CustomTypeAttribute
