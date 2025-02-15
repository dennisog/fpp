// ======================================================================
// \title  Enum2ArrayAc.cpp
// \author Generated by fpp-to-cpp
// \brief  cpp file for Enum2 array
// ======================================================================

#include <cstdio>
#include <cstring>

#include "Enum2ArrayAc.hpp"
#include "Fw/Types/Assert.hpp"
#include "Fw/Types/StringUtils.hpp"

// ----------------------------------------------------------------------
// Constructors
// ----------------------------------------------------------------------

Enum2 ::
  Enum2() :
    Serializable()
{
  // Construct using element-wise constructor
  *this = Enum2(
    E2::C,
    E2::C,
    E2::C,
    E2::C,
    E2::C
  );
}

Enum2 ::
  Enum2(const ElementType (&a)[SIZE]) :
    Serializable()
{
  for (U32 index = 0; index < SIZE; index++) {
    this->elements[index] = a[index];
  }
}

Enum2 ::
  Enum2(const ElementType& e) :
    Serializable()
{
  for (U32 index = 0; index < SIZE; index++) {
    this->elements[index] = e;
  }
}

Enum2 ::
  Enum2(
      const ElementType& e1,
      const ElementType& e2,
      const ElementType& e3,
      const ElementType& e4,
      const ElementType& e5
  ) :
    Serializable()
{
  this->elements[0] = e1;
  this->elements[1] = e2;
  this->elements[2] = e3;
  this->elements[3] = e4;
  this->elements[4] = e5;
}

Enum2 ::
  Enum2(const Enum2& obj) :
    Serializable()
{
  for (U32 index = 0; index < SIZE; index++) {
    this->elements[index] = obj.elements[index];
  }
}

// ----------------------------------------------------------------------
// Operators
// ----------------------------------------------------------------------

Enum2::ElementType& Enum2 ::
  operator[](const U32 i)
{
  FW_ASSERT(i < SIZE);
  return this->elements[i];
}

const Enum2::ElementType& Enum2 ::
  operator[](const U32 i) const
{
  FW_ASSERT(i < SIZE);
  return this->elements[i];
}

Enum2& Enum2 ::
  operator=(const Enum2& obj)
{
  if (this == &obj) {
    return *this;
  }

  for (U32 index = 0; index < SIZE; index++) {
    this->elements[index] = obj.elements[index];
  }
  return *this;
}

Enum2& Enum2 ::
  operator=(const ElementType (&a)[SIZE])
{
  for (U32 index = 0; index < SIZE; index++) {
    this->elements[index] = a[index];
  }
  return *this;
}

Enum2& Enum2 ::
  operator=(const ElementType& e)
{
  for (U32 index = 0; index < SIZE; index++) {
    this->elements[index] = e;
  }
  return *this;
}

bool Enum2 ::
  operator==(const Enum2& obj) const
{
  for (U32 index = 0; index < SIZE; index++) {
    if (!((*this)[index] == obj[index])) {
      return false;
    }
  }
  return true;
}

bool Enum2 ::
  operator!=(const Enum2& obj) const
{
  return !(*this == obj);
}

#ifdef BUILD_UT

std::ostream& operator<<(std::ostream& os, const Enum2& obj) {
  Fw::String s;
  obj.toString(s);
  os << s;
  return os;
}

#endif

// ----------------------------------------------------------------------
// Member functions
// ----------------------------------------------------------------------

Fw::SerializeStatus Enum2 ::
  serialize(Fw::SerializeBufferBase& buffer) const
{
  Fw::SerializeStatus status = Fw::FW_SERIALIZE_OK;
  for (U32 index = 0; index < SIZE; index++) {
    status = buffer.serialize((*this)[index]);
    if (status != Fw::FW_SERIALIZE_OK) {
      return status;
    }
  }
  return status;
}

Fw::SerializeStatus Enum2 ::
  deserialize(Fw::SerializeBufferBase& buffer)
{
  Fw::SerializeStatus status = Fw::FW_SERIALIZE_OK;
  for (U32 index = 0; index < SIZE; index++) {
    status = buffer.deserialize((*this)[index]);
    if (status != Fw::FW_SERIALIZE_OK) {
      return status;
    }
  }
  return status;
}

#if FW_ARRAY_TO_STRING || BUILD_UT

void Enum2 ::
  toString(Fw::StringBase& sb) const
{
  static const char *formatString = "[ "
    "%s "
    "%s "
    "%s "
    "%s "
    "%s ]";

  // Declare strings to hold any serializable toString() arguments
  Fw::String str0;
  Fw::String str1;
  Fw::String str2;
  Fw::String str3;
  Fw::String str4;

  // Call toString for arrays and serializable types
  this->elements[0].toString(str0);
  this->elements[1].toString(str1);
  this->elements[2].toString(str2);
  this->elements[3].toString(str3);
  this->elements[4].toString(str4);

  char outputString[FW_ARRAY_TO_STRING_BUFFER_SIZE];
  (void) snprintf(
    outputString,
    FW_ARRAY_TO_STRING_BUFFER_SIZE,
    formatString,
    str0.toChar(),
    str1.toChar(),
    str2.toChar(),
    str3.toChar(),
    str4.toChar()
  );

  outputString[FW_ARRAY_TO_STRING_BUFFER_SIZE-1] = 0; // NULL terminate
  sb = outputString;
}

#endif
