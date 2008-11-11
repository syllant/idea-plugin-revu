package com.sample;

/**
 * Created by IntelliJ IDEA.
 * User: xxxxxx
 */
public class ClassB
{
  private String fieldA;
  private String fieldB;
  private String fieldC;
  private String fieldD;

  public String getFieldA()
  {
    return fieldA;
  }

  public void setFieldA(String fieldA)
  {
    this.fieldA = fieldA;
  }

  public String getFieldB()
  {
    return fieldB;
  }

  public void setFieldB(String fieldB)
  {
    this.fieldB = fieldB;
  }

  public String getFieldC()
  {
    return fieldC;
  }

  public void setFieldC(String fieldC)
  {
    this.fieldC = fieldC;
  }

  public String getFieldD()
  {
    return fieldD;
  }

  public void setFieldD(String fieldD)
  {
    this.fieldD = fieldD;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ClassB classA = (ClassB) o;

    if (fieldA != null ? !fieldA.equals(classA.fieldA) : classA.fieldA != null) return false;
    if (fieldB != null ? !fieldB.equals(classA.fieldB) : classA.fieldB != null) return false;
    if (fieldC != null ? !fieldC.equals(classA.fieldC) : classA.fieldC != null) return false;
    if (fieldD != null ? !fieldD.equals(classA.fieldD) : classA.fieldD != null) return false;

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = fieldA != null ? fieldA.hashCode() : 0;
    result = 31 * result + (fieldB != null ? fieldB.hashCode() : 0);
    result = 31 * result + (fieldC != null ? fieldC.hashCode() : 0);
    result = 31 * result + (fieldD != null ? fieldD.hashCode() : 0);
    return result;
  }

}