package no.geosoft.common.geometry;

/**
 * Implementation of a 4x4 matrix suited for use in a 2D and 3D
 * graphics rendering engine.
 *
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
public final class Matrix4x4
{
  private final double[] m_ = new double[16];

  /**
   * Construct a 4x4 identity matrix.
   */
  public Matrix4x4()
  {
    setIdentity();
  }

  /**
   * Construct a 4x4 matrix with the specified element values.
   *
   * @param m  Array of 16 values corresponding to m00, m01, ..., m33.
   * @throws IllegalArgumentException  If m is null or of wrong length.
   */
  public Matrix4x4(double[] m)
  {
    if (m == null)
      throw new IllegalArgumentException("m cannot be null");

    if (m.length != 16)
      throw new IllegalArgumentException("Invalid array length: " + m.length);

    set(m);
  }

  /**
   * Constrauct a 4x4 matrix as a copy of the specified matrix.
   *
   * @param matrix  Matrix to copy. Non-null.
   * @throws IllegalArgumentException  If matrix is null.
   */
  public Matrix4x4(Matrix4x4 matrix)
  {
    if (matrix == null)
      throw new IllegalArgumentException("matrix cannot be null");

    set(matrix);
  }

  /**
   * Construct a 4x4 matrix with the specified values.
   *
   * @param m00  Value of element m[0,0].
   * @param m01  Value of element m[0,1].
   * @param m02  Value of element m[0,2].
   * @param m03  Value of element m[0,3].
   * @param m10  Value of element m[1,0].
   * @param m11  Value of element m[1,1].
   * @param m12  Value of element m[1,2].
   * @param m13  Value of element m[1,3].
   * @param m20  Value of element m[2,0].
   * @param m21  Value of element m[2,1].
   * @param m22  Value of element m[2,2].
   * @param m23  Value of element m[2,3].
   * @param m30  Value of element m[3,0].
   * @param m31  Value of element m[3,1].
   * @param m32  Value of element m[3,2].
   * @param m33  Value of element m[3,3].
   */
  public Matrix4x4(double m00, double m01, double m02, double m03,
                   double m10, double m11, double m12, double m13,
                   double m20, double m21, double m22, double m23,
                   double m30, double m31, double m32, double m33)
  {
    set(m00, m01, m02, m03,
        m10, m11, m12, m13,
        m20, m21, m22, m23,
        m30, m31, m32, m33);
  }

  /**
   * Make an identity matrix out of this 4x4 matrix.
   */
  public void setIdentity()
  {
    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        m_[i * 4 + j] = i == j ? 1.0 : 0.0;
  }

  /**
   * Set the value of this 4x4matrix according to the specified matrix.
   *
   * @param matrix  Matrix to copy. Non-null.
   * @throws IllegalArgumentException  If matrix is null.
   */
  public void set(Matrix4x4 matrix)
  {
    if (matrix == null)
      throw new IllegalArgumentException("matrix cannot be null");

    for (int i = 0; i < 16; i++)
      m_[i] = matrix.m_[i];
  }

  /**
   * Set the values of this 4x4 matrix.
   *
   * @param m  Array of 16 values corresponding to m00, m01, ..., m33.
   * @throws IllegalArgumentException  If m is null or of wrong length.
   */
  public void set(double[] m)
  {
    if (m == null)
      throw new IllegalArgumentException("m cannot be null");

    if (m.length != 16)
      throw new IllegalArgumentException("Invalid array length: " + m.length);

    for (int i = 0; i < 16; i++)
      m_[i] = m[i];
  }

  /**
   * Set the values of this 4x4 matrix.
   *
   * @param m00  Value of element m[0,0].
   * @param m01  Value of element m[0,1].
   * @param m02  Value of element m[0,2].
   * @param m03  Value of element m[0,3].
   * @param m10  Value of element m[1,0].
   * @param m11  Value of element m[1,1].
   * @param m12  Value of element m[1,2].
   * @param m13  Value of element m[1,3].
   * @param m20  Value of element m[2,0].
   * @param m21  Value of element m[2,1].
   * @param m22  Value of element m[2,2].
   * @param m23  Value of element m[2,3].
   * @param m30  Value of element m[3,0].
   * @param m31  Value of element m[3,1].
   * @param m32  Value of element m[3,2].
   * @param m33  Value of element m[3,3].
   */
  public void set(double m00, double m01, double m02, double m03,
                  double m10, double m11, double m12, double m13,
                  double m20, double m21, double m22, double m23,
                  double m30, double m31, double m32, double m33)
  {
    m_[0]  = m00;
    m_[1]  = m01;
    m_[2]  = m02;
    m_[3]  = m03;

    m_[4]  = m10;
    m_[5]  = m11;
    m_[6]  = m12;
    m_[7]  = m13;

    m_[8]  = m20;
    m_[9]  = m21;
    m_[10] = m22;
    m_[11] = m23;

    m_[12] = m30;
    m_[13] = m31;
    m_[14] = m32;
    m_[15] = m33;
  }

  /**
   * Return (a copy of) the values of this 4x4 matrix.
   *
   * @return  Array of 16: m00, m01, etc.
   */
  public double[] get()
  {
    double[] m = new double[16];
    for (int i = 0; i < 16; i++)
      m[i] = m_[i];

    return m;
  }

  /**
   * Return matrix element [i,j].
   *
   * @param i  Row of element to get. [0,3].
   * @param j  Column of element to get. [0,3].
   * @return   Requested element value.
   * @throws   IllegalArgumentException  If 1 or j is out of bounds.
   */
  public double getElement(int i, int j)
  {
    if (i < 0 || i > 3)
      throw new IllegalArgumentException("Invalid i: " + i);

    if (j < 0 || j> 3)
      throw new IllegalArgumentException("Invalid j: " + j);

    return m_[i * 4 + j];
  }

  /**
   * Set specified matrix element.
   *
   * @param i      Row of element to set. [0,3].
   * @param j      Column of element to set [0,3].
   * @param value  New element value.
   * @throws       IllegalArgumentException  If 1 or j is out of bounds.
   */
  public void setElement(int i, int j, double value)
  {
    if (i < 0 || i > 3)
      throw new IllegalArgumentException("Invalid i: " + i);

    if (j < 0 || j> 3)
      throw new IllegalArgumentException("Invalid j: " + j);

    m_[i * 4 + j] = value;
  }

  /**
   * Add the specified 4x4 matrix to this matrix.
   *
   * @param matrix  Matrix to add. Non-null.
   * @return        This matrix. Never null.
   * @throws IllegalArgumentException  If matrix is null.
   */
  public Matrix4x4 add(Matrix4x4 matrix)
  {
    if (matrix == null)
      throw new IllegalArgumentException("matrix cannot be null");

    for (int i = 0; i < 4; i++)
      for (int j = 0; j < 4; j++)
        m_[i * 4 + j] += matrix.m_[i * 4 + j];

    return this;
  }

  /**
   * Add two matrices and return the result matrix.
   *
   * @param m1  First matrix to add. Non-null.
   * @param m2  Second matrix to add. Non-null.
   * @return    Sum m1 + m2.
   * @throws IllegalArgumentException  If m1 or m2 is null.
   */
  public static Matrix4x4 add(Matrix4x4 m1, Matrix4x4 m2)
  {
    if (m1 == null)
      throw new IllegalArgumentException("m1 cannot be null");

    if (m2 == null)
      throw new IllegalArgumentException("m2 cannot be null");

    Matrix4x4 m = new Matrix4x4(m1);
    m.add(m2);
    return m;
  }

  /**
   * Multiply this 4x4 matrix with the specified matrix and
   * store the result in this 4x4 matrix.
   *
   * @param matrix  Matrix to multiply with.
   * @return        This matrix. Never null.
   * @throws IllegalArgumentException  If matrix is null.
   */
  public Matrix4x4 multiply(Matrix4x4 matrix)
  {
    if (matrix == null)
      throw new IllegalArgumentException("m1 cannot be null");

    Matrix4x4 product = new Matrix4x4();

    for (int i = 0; i < 16; i += 4) {
      for (int j = 0; j < 4; j++) {
        product.m_[i + j] = 0.0;
        for (int k = 0; k < 4; k++)
          product.m_[i + j] += m_[i + k] * matrix.m_[k * 4 + j];
      }
    }

    set(product);

    return this;
  }

  /**
   * Multiply two matrices and return the result matrix.
   *
   * @param m1  First matrix to multiply. Non-null.
   * @param m2  Second matrix to multiply. Non-null.
   * @return    Product m1 * m2.
   * @throws IllegalArgumentException  If m1 or m2 is null.
   */
  public static Matrix4x4 multiply(Matrix4x4 m1, Matrix4x4 m2)
  {
    if (m1 == null)
      throw new IllegalArgumentException("m1 cannot be null");

    if (m2 == null)
      throw new IllegalArgumentException("m2 cannot be null");

    Matrix4x4 m = new Matrix4x4(m1);
    m.multiply(m2);
    return m;
  }

  /**
   * Multiply this 4x4 matrix with the specified vector.
   *
   * @param vector4  Vector to multiply with. Non-null.
   * @return         Result of operation.
   * @throws IllegalArgumentException  If vector4 is null.
   */
  public Vector4 multiply(Vector4 vector4)
  {
    if (vector4 == null)
      throw new IllegalArgumentException("vector4 cannot be null");

    Vector4  product = new Vector4();

    for (int i = 0; i < 4; i++) {
      double value = 0.0;
      for (int j = 0; j < 4; j++)
        value += getElement(i, j) * vector4.getElement(j);
      product.setElement(i, value);
    }

    return product;
  }

  /**
   * Transform one coordinate using this 4x4 matrix.
   *
   * @param point  [x0,y0,z0]. Array of three. Non-null.
   * @return       Result of operation: [x0',y0',z0']. Array of three.
   * @throws IllegalArgumentException  If point is null or not of length 3.
   */
  public double[] transformPoint(double[] point)
  {
    if (point == null)
      throw new IllegalArgumentException("point cannot be null");

    if (point.length != 3)
      throw new IllegalArgumentException("Invalid point length: " + point.length);

    double[] result = new double[3];

    result[0] = point[0] * m_[0]  +
                point[1] * m_[4]  +
                point[2] * m_[8]  + m_[12];

    result[1] = point[0] * m_[1]  +
                point[1] * m_[5]  +
                point[2] * m_[9]  + m_[13];

    result[2] = point[0] * m_[2]   +
                point[1] * m_[6]   +
                point[2] * m_[10]  + m_[14];

    return result;
  }

  /**
   * Transform a set of 3D coordinates using this 4x4 matrix.
   * The result of the operation is put back in the original array.
   *
   * @param points  Points to transform [x0,y0,z0,x1,y1,z1,...].
   *                Length must be multiple of 3. Non-null.
   * @throws IllegalArgumentException  If points is null or not multiple of 3.
   */
  public void transformPoints(double[] points)
  {
    if (points == null)
      throw new IllegalArgumentException("point cannot be null");

    if (points.length % 3 != 0)
      throw new IllegalArgumentException("Invalid points length: " + points.length);

    for (int i = 0; i < points.length; i += 3) {
      double x = points[i + 0] * m_[0]  +
                 points[i + 1] * m_[4]  +
                 points[i + 2] * m_[8]  + m_[12];

      double y = points[i + 0] * m_[1]  +
                 points[i + 1] * m_[5]  +
                 points[i + 2] * m_[9]  + m_[13];

      double z = points[i + 0] * m_[2]   +
                 points[i + 1] * m_[6]   +
                 points[i + 2] * m_[10]  + m_[14];

      points[i + 0] = x;
      points[i + 1] = y;
      points[i + 2] = z;
    }
  }

  /**
   * Transform a set of 2D (x,y) coordinates using this 4x4 matrix.
   * The result of the operation is put back in the original array
   * rounded to the nearest integer.
   *
   * @param points  Points to transform [x0,y0,x1,y1,...].
   *                Multiple of 2. Non-null.
   * @throws IllegalArgumentException  If points is null or not multiple of 2.
   */
  public void transformXyPoints(double[] points)
  {
    if (points == null)
      throw new IllegalArgumentException("point cannot be null");

    if (points.length % 2 != 0)
      throw new IllegalArgumentException("Invalid points length: " + points.length);

    for (int i = 0; i < points.length; i += 2) {
      double x = points[i + 0] * m_[0]  +
                 points[i + 1] * m_[4]  + m_[12];

      double y = points[i + 0] * m_[1]  +
                 points[i + 1] * m_[5]  + m_[13];

      points[i + 0] = x;
      points[i + 1] = y;
    }
  }

  /**
   * Transform a set of 3D coordinates using this 4x4 matrix.
   * The result of the operation is put back in the original array.
   *
   * @param points  Points to transform [x0,y0,z0,x1,y1,z1,...].
   *                Multiple of 3. Non-null.
   * @throws IllegalArgumentException  If points is null or not multiple of 3.
   */
  public void transformPoints(int[] points)
  {
    if (points == null)
      throw new IllegalArgumentException("point cannot be null");

    if (points.length % 3 != 0)
      throw new IllegalArgumentException("Invalid points length: " + points.length);

    for (int i = 0; i < points.length; i += 3) {
      double x = points[i + 0] * m_[0]  +
                 points[i + 1] * m_[4]  +
                 points[i + 2] * m_[8]  + m_[12];

      double y = points[i + 0] * m_[1]  +
                 points[i + 1] * m_[5]  +
                 points[i + 2] * m_[9]  + m_[13];

      double z = points[i + 0] * m_[2]  +
                 points[i + 1] * m_[6]  +
                 points[i + 2] * m_[10] + m_[14];

      points[i + 0] = (int) Math.round(x);
      points[i + 1] = (int) Math.round(y);
      points[i + 2] = (int) Math.round(z);
    }
  }

  /**
   * Transform a set of 2D (x,y) coordinates using this 4x4 matrix.
   * The result of the operation is put back in the original array
   * rounded to the nearest integer.
   *
   * @param points  Points to transform [x0,y0,x1,y1,...].
   *                Length must be multiple of 2. Non-null.
   * @throws IllegalArgumentException  If points is null or not multiple of 2.
   */
  public void transformXyPoints(int[] points)
  {
    if (points == null)
      throw new IllegalArgumentException("point cannot be null");

    if (points.length % 2 != 0)
      throw new IllegalArgumentException("Invalid points length: " + points.length);

    for (int i = 0; i < points.length; i += 2) {
      double x = points[i + 0] * m_[0] +
                 points[i + 1] * m_[4] + m_[12];

      double y = points[i + 0] * m_[1]  +
                 points[i + 1] * m_[5]  + m_[13];

      points[i + 0] = (int) Math.round(x);
      points[i + 1] = (int) Math.round(y);
    }
  }

  /**
   * Apply specified translation to this 4x4 matrix.
   *
   * @param dx  x translation.
   * @param dy  y translation.
   * @param dz  z translation.
   * @return    This matrix. Never null.
   */
  public Matrix4x4 translate(double dx, double dy, double dz)
  {
    Matrix4x4 translationMatrix = new Matrix4x4();

    translationMatrix.setElement(3, 0, dx);
    translationMatrix.setElement(3, 1, dy);
    translationMatrix.setElement(3, 2, dz);

    return multiply(translationMatrix);
  }

  /**
   * Apply specified XY translation to this 4x4 matrix.
   *
   * @param dx  x translation.
   * @param dy  y translation.
   * @return    This matrix. Never null.
   */
  public Matrix4x4 translate(double dx, double dy)
  {
    return translate(dx, dy, 0.0);
  }

  /**
   * Apply rotation around X axis to this matrix.
   *
   * @param angle  Angle to rotate in radians.
   * @return       This matrix. Never null.
   */
  public Matrix4x4 rotateX(double angle)
  {
    Matrix4x4 rotationMatrix = new Matrix4x4();

    double cosAngle = Math.cos(angle);
    double sinAngle = Math.sin(angle);

    rotationMatrix.setElement(1, 1,  cosAngle);
    rotationMatrix.setElement(1, 2,  sinAngle);
    rotationMatrix.setElement(2, 1, -sinAngle);
    rotationMatrix.setElement(2, 2,  cosAngle);

    return multiply(rotationMatrix);
  }

  /**
   * Apply rotation around Y axis to this matrix.
   *
   * @param angle  Angle to rotate in radians.
   * @return       This matrix. Never null.
   */
  public Matrix4x4 rotateY(double angle)
  {
    Matrix4x4 rotationMatrix = new Matrix4x4();

    double cosAngle = Math.cos(angle);
    double sinAngle = Math.sin(angle);

    rotationMatrix.setElement(0, 0,  cosAngle);
    rotationMatrix.setElement(0, 2, -sinAngle);
    rotationMatrix.setElement(2, 0,  sinAngle);
    rotationMatrix.setElement(2, 2,  cosAngle);

    return multiply(rotationMatrix);
  }

  /**
   * Apply rotation around z axis to this matrix.
   *
   * @param angle  Angle to rotate in radians.
   * @return       This matrix. Never null.
   */
  public Matrix4x4 rotateZ(double angle)
  {
    Matrix4x4 rotationMatrix = new Matrix4x4();

    double cosAngle = Math.cos(angle);
    double sinAngle = Math.sin(angle);

    rotationMatrix.setElement(0, 0,  cosAngle);
    rotationMatrix.setElement(0, 1,  sinAngle);
    rotationMatrix.setElement(1, 0, -sinAngle);
    rotationMatrix.setElement(1, 1,  cosAngle);

    return multiply(rotationMatrix);
  }

  /**
   * Apply rotation around an arbitrary axis.
   * <p>
   * Ref: http://www.swin.edu.au/astronomy/pbourke/geometry/rotate/
   * (but be aware of errors, corrected here)
   *
   * @param angle  Angle to rotate [radians]
   * @param p0     First point defining the axis (x,y,z). Non-null.
   * @param p1     Second point defining the axis (x,y,z). Non-null.
   * @return       This matrix. Never null.
   * @throws IllegalArgumentException  If p0 or p1 is null or not of length 3.
   */
  public Matrix4x4 rotate(double angle, double[] p0, double[] p1)
  {
    if (p0 == null)
      throw new IllegalArgumentException("p0 cannot be null");

    if (p0.length != 3)
      throw new IllegalArgumentException("Invalid p0 length: " + p0.length);

    if (p1 == null)
      throw new IllegalArgumentException("p1 cannot be null");

    if (p1.length != 3)
      throw new IllegalArgumentException("Invalid p1 length: " + p1.length);

    // Represent axis of rotation by a unit vector [a,b,c]
    double a = p1[0] - p0[0];
    double b = p1[1] - p0[1];
    double c = p1[2] - p0[2];

    double length = Math.sqrt(a*a + b*b + c*c);

    a /= length;
    b /= length;
    c /= length;

    double d = Math.sqrt(b*b + c*c);

    // Coefficients used for step 2 matrix
    double e = d == 0.0 ? 1.0 : c / d;
    double f = d == 0.0 ? 0.0 : b / d;

    // Coefficients used for the step 3 matrix
    double k = d;
    double l = a;

    // Coefficients for the step 5 matrix (inverse of step 3)
    double m = d / (a*a + d*d);
    double n = a / (a*a + d*d);

    // Coefficients for the step 4 matrix
    double cosAngle = Math.cos(angle);
    double sinAngle = Math.sin(angle);

    //
    // Step 1
    //
    Matrix4x4 step1 = new Matrix4x4();
    step1.setElement(3, 0, -p0[0]);
    step1.setElement(3, 1, -p0[1]);
    step1.setElement(3, 2, -p0[2]);

    //
    // Step 2
    //
    Matrix4x4 step2 = new Matrix4x4();
    step2.setElement(1, 1,  e);
    step2.setElement(1, 2,  f);
    step2.setElement(2, 1, -f);
    step2.setElement(2, 2,  e);

    //
    // Step 3
    //
    Matrix4x4 step3 = new Matrix4x4();
    step3.setElement(0, 0,  k);
    step3.setElement(0, 2,  l);
    step3.setElement(2, 0, -l);
    step3.setElement(2, 2,  k);

    //
    // Step 4
    //
    Matrix4x4 step4 = new Matrix4x4();
    step4.setElement(0, 0,  cosAngle);
    step4.setElement(0, 1,  sinAngle);
    step4.setElement(1, 0, -sinAngle);
    step4.setElement(1, 1,  cosAngle);

    //
    // Step 5 (inverse of step 3)
    //
    Matrix4x4 step5 = new Matrix4x4();
    step5.setElement(0, 0,  m);
    step5.setElement(0, 2, -n);
    step5.setElement(2, 0,  n);
    step5.setElement(2, 2,  m);

    //
    // Step 6 (inverse of step 2)
    //
    Matrix4x4 step6 = new Matrix4x4();
    step6.setElement(1, 1,  e);
    step6.setElement(1, 2, -f);
    step6.setElement(2, 1,  f);
    step6.setElement(2, 2,  e);

    //
    // Step 7 (inverse of step 1)
    //
    Matrix4x4 step7 = new Matrix4x4();
    step7.setElement(3, 0, p0[0]);
    step7.setElement(3, 1, p0[1]);
    step7.setElement(3, 2, p0[2]);

    multiply(step1);
    multiply(step2);
    multiply(step3);
    multiply(step4);
    multiply(step5);
    multiply(step6);
    multiply(step7);

    return this;
  }

  /**
   * Apply scaling (relative to origo) to this 4x4 matrix.
   *
   * @param xScale  Scaling in x direction.
   * @param yScale  Scaling in y direction.
   * @param zScale  Scaling in z direction.
   * @return        This matrix. Never null.
   */
  public Matrix4x4 scale(double xScale, double yScale, double zScale)
  {
    Matrix4x4 scalingMatrix = new Matrix4x4();

    scalingMatrix.setElement(0, 0, xScale);
    scalingMatrix.setElement(1, 1, yScale);
    scalingMatrix.setElement(2, 2, zScale);

    return multiply(scalingMatrix);
  }

  /**
   * Apply scaling relative to a fixed point to this 4x4 matrix.
   *
   * @param xScale      Scaling in x direction.
   * @param yScale      Scaling in y direction.
   * @param zScale      Scaling in z direction.
   * @param fixedPoint  Scaling origo [x,y,z]. Non-null.
   * @return            This matrix. Never null.
   * @throws IllegalArgumentException  If fixedPoint is null or
   *                    of wrong length.
   */
  public Matrix4x4 scale(double xScale, double yScale, double zScale,
                         double[] fixedPoint)
  {
    if (fixedPoint == null)
      throw new IllegalArgumentException("fixedPoint cannot be null");

    if (fixedPoint.length != 3)
      throw new IllegalArgumentException("Invalid fixed point length: " +
                                        fixedPoint.length);

    Matrix4x4 step1 = new Matrix4x4();
    step1.translate(-fixedPoint[0], -fixedPoint[1], -fixedPoint[2]);

    Matrix4x4 step2 = new Matrix4x4();
    step2.scale(xScale, yScale, zScale);

    Matrix4x4 step3 = new Matrix4x4();
    step3.translate(fixedPoint[0], fixedPoint[1], fixedPoint[2]);

    multiply(step1);
    multiply(step2);
    multiply(step3);

    return this;
  }

  /**
   * Invert this 4x4 matrix.
   *
   * @return  This matrix. Never null.
   */
  public Matrix4x4 invert()
  {
    double[] tmp = new double[12];
    double[] src = new double[16];
    double[] dst = new double[16];

    // Transpose matrix
    for (int i = 0; i < 4; i++) {
      src[i +  0] = m_[i*4 + 0];
      src[i +  4] = m_[i*4 + 1];
      src[i +  8] = m_[i*4 + 2];
      src[i + 12] = m_[i*4 + 3];
    }

    // Calculate pairs for first 8 elements (cofactors)
    tmp[0] = src[10] * src[15];
    tmp[1] = src[11] * src[14];
    tmp[2] = src[9]  * src[15];
    tmp[3] = src[11] * src[13];
    tmp[4] = src[9]  * src[14];
    tmp[5] = src[10] * src[13];
    tmp[6] = src[8]  * src[15];
    tmp[7] = src[11] * src[12];
    tmp[8] = src[8]  * src[14];
    tmp[9] = src[10] * src[12];
    tmp[10] = src[8] * src[13];
    tmp[11] = src[9] * src[12];

    // Calculate first 8 elements (cofactors)
    dst[0]  = tmp[0]*src[5] + tmp[3]*src[6] + tmp[4]*src[7];
    dst[0] -= tmp[1]*src[5] + tmp[2]*src[6] + tmp[5]*src[7];
    dst[1]  = tmp[1]*src[4] + tmp[6]*src[6] + tmp[9]*src[7];
    dst[1] -= tmp[0]*src[4] + tmp[7]*src[6] + tmp[8]*src[7];
    dst[2]  = tmp[2]*src[4] + tmp[7]*src[5] + tmp[10]*src[7];
    dst[2] -= tmp[3]*src[4] + tmp[6]*src[5] + tmp[11]*src[7];
    dst[3]  = tmp[5]*src[4] + tmp[8]*src[5] + tmp[11]*src[6];
    dst[3] -= tmp[4]*src[4] + tmp[9]*src[5] + tmp[10]*src[6];
    dst[4]  = tmp[1]*src[1] + tmp[2]*src[2] + tmp[5]*src[3];
    dst[4] -= tmp[0]*src[1] + tmp[3]*src[2] + tmp[4]*src[3];
    dst[5]  = tmp[0]*src[0] + tmp[7]*src[2] + tmp[8]*src[3];
    dst[5] -= tmp[1]*src[0] + tmp[6]*src[2] + tmp[9]*src[3];
    dst[6]  = tmp[3]*src[0] + tmp[6]*src[1] + tmp[11]*src[3];
    dst[6] -= tmp[2]*src[0] + tmp[7]*src[1] + tmp[10]*src[3];
    dst[7]  = tmp[4]*src[0] + tmp[9]*src[1] + tmp[10]*src[2];
    dst[7] -= tmp[5]*src[0] + tmp[8]*src[1] + tmp[11]*src[2];

    // Calculate pairs for second 8 elements (cofactors)
    tmp[0]  = src[2]*src[7];
    tmp[1]  = src[3]*src[6];
    tmp[2]  = src[1]*src[7];
    tmp[3]  = src[3]*src[5];
    tmp[4]  = src[1]*src[6];
    tmp[5]  = src[2]*src[5];
    tmp[6]  = src[0]*src[7];
    tmp[7]  = src[3]*src[4];
    tmp[8]  = src[0]*src[6];
    tmp[9]  = src[2]*src[4];
    tmp[10] = src[0]*src[5];
    tmp[11] = src[1]*src[4];

    // Calculate second 8 elements (cofactors)
    dst[8]   = tmp[0] * src[13]  + tmp[3] * src[14]  + tmp[4] * src[15];
    dst[8]  -= tmp[1] * src[13]  + tmp[2] * src[14]  + tmp[5] * src[15];
    dst[9]   = tmp[1] * src[12]  + tmp[6] * src[14]  + tmp[9] * src[15];
    dst[9]  -= tmp[0] * src[12]  + tmp[7] * src[14]  + tmp[8] * src[15];
    dst[10]  = tmp[2] * src[12]  + tmp[7] * src[13]  + tmp[10]* src[15];
    dst[10] -= tmp[3] * src[12]  + tmp[6] * src[13]  + tmp[11]* src[15];
    dst[11]  = tmp[5] * src[12]  + tmp[8] * src[13]  + tmp[11]* src[14];
    dst[11] -= tmp[4] * src[12]  + tmp[9] * src[13]  + tmp[10]* src[14];
    dst[12]  = tmp[2] * src[10]  + tmp[5] * src[11]  + tmp[1] * src[9];
    dst[12] -= tmp[4] * src[11]  + tmp[0] * src[9]   + tmp[3] * src[10];
    dst[13]  = tmp[8] * src[11]  + tmp[0] * src[8]   + tmp[7] * src[10];
    dst[13] -= tmp[6] * src[10]  + tmp[9] * src[11]  + tmp[1] * src[8];
    dst[14]  = tmp[6] * src[9]   + tmp[11]* src[11]  + tmp[3] * src[8];
    dst[14] -= tmp[10]* src[11 ] + tmp[2] * src[8]   + tmp[7] * src[9];
    dst[15]  = tmp[10]* src[10]  + tmp[4] * src[8]   + tmp[9] * src[9];
    dst[15] -= tmp[8] * src[9]   + tmp[11]* src[10]  + tmp[5] * src[8];

    // Calculate determinant
    double det = src[0]*dst[0] + src[1]*dst[1] + src[2]*dst[2] + src[3]*dst[3];

    // Calculate matrix inverse
    det = 1.0 / det;
    for (int i = 0; i < 16; i++)
      m_[i] = dst[i] * det;

    return this;
  }

  /**
   * Return the inverse of the specified matrix.
   *
   * @param matrix  Matrix to find the inverse of. Non-null.
   * @return        Inverse of the specified matrix.
   * @throws IllegalArgumentException  If matrix is null.
   */
  public static Matrix4x4 inverse(Matrix4x4 matrix)
  {
    if (matrix == null)
      throw new IllegalArgumentException("matrix cannot be null");

    Matrix4x4 m = new Matrix4x4(matrix);
    m.invert();
    return m;
  }

  /**
   * Solve the A x = b equation, where A is this 4x4 matrix, b is the
   * specified result vector and the returned vector is the unknown x.
   *
   * @param vector  Result vector. Non-null.
   * @return        Requested x vector. Never null.
   * @throws IllegalArgumentException  If vector is null.
   */
  public Vector4 solve(Vector4 vector)
  {
    if (vector == null)
      throw new IllegalArgumentException("vector cannot be null");

    Matrix4x4 inverse = new Matrix4x4(this);
    inverse.invert();
    Vector4 result = inverse.multiply(vector);
    return result;
  }

  /**
   * Make this 4x4 matrix a world-2-device transformation matrix.
   * <p>
   * The world system is defined as follows:
   *
   * <pre>
   *        w2 o
   *           |
   *           |
   *           |
   *        w0 o-------o w1
   * </pre>
   * <p>
   * Each point is defined with x,y,z so this system may in effect be
   * arbitrary oriented in space, and may include sharing.
   * <p>
   * The device system is defined as follows:
   *
   * <pre>
   *             width
   *     x0,y0 o-------o
   *           |
   *    height |
   *           |
   *           o
   * </pre>
   * <p>
   * The matrix maps w2 to (x0,y0), w0 to the lower left corner of the
   * device rectangle, and w1 to the lower right corner of the device
   * rectangle.
   *
   * @param w0      x,y,z coordinate of first world position. Non-null.
   * @param w1      x,y,z coordinate of second world position. Non-null.
   * @param w2      x,y,z coordinate of third world position. Non-null.
   * @param x0      X coordinate of upper left corner of device.
   * @param y0      Y coordinate of upper left corner of device.
   * @param width   Width of device
   * @param height  Height of device.
   */
  public void setWorld2DeviceTransform(double[] w0, double[] w1, double[] w2,
                                       int x0, int y0, int width, int height)
  {
    if (w0 == null)
      throw new IllegalArgumentException("w0 cannot be null");

    if (w0.length != 3)
      throw new IllegalArgumentException("Invalid w0 length: " +  w0.length);

    if (w1 == null)
      throw new IllegalArgumentException("w1 cannot be null");

    if (w1.length != 3)
      throw new IllegalArgumentException("Invalid w1 length: " +  w1.length);

    if (w2 == null)
      throw new IllegalArgumentException("w2 cannot be null");

    if (w2.length != 3)
      throw new IllegalArgumentException("Invalid w2 length: " +  w2.length);

    setIdentity();

    double[] x = new double[4];
    double[] y = new double[4];
    double[] z = new double[4];

    // Make direction vectors for new system
    x[0] = w2[0];          y[0] = w2[1];          z[0] = w2[2];
    x[1] = w1[0] - w0[0];  y[1] = w1[1] - w0[1];  z[1] = w1[2] - w0[2];
    x[2] = w0[0] - w2[0];  y[2] = w0[1] - w2[1];  z[2] = w0[2] - w2[2];

    x[3] = y[1]*z[2] - z[1]*y[2];
    y[3] = z[1]*x[2] - x[1]*z[2];
    z[3] = x[1]*y[2] - y[1]*x[2];

    // Normalize new z-vector, in case someone needs
    // new z-value in addition to device coordinates */
    double length = Math.sqrt(x[3]*x[3] + y[3]*y[3] + z[3]*z[3]);
    x[3] /= length;
    y[3] /= length;
    z[3] /= length;

    // Translate back to new origin
    translate(-x[0], -y[0], -z[0]);

    // Multiply with inverse of definition of new coordinate system
    double a = y[2]*z[3] - z[2]*y[3];
    double b = z[1]*y[3] - y[1]*z[3];
    double c = y[1]*z[2] - z[1]*y[2];

    double det = x[1]*a + x[2]*b + x[3]*c;

    double[] m = new double[16];

    m[0]  = a / det;
    m[1]  = b / det;
    m[2]  = c / det;
    m[3]  = 0.0;

    m[4]  = (x[3]*z[2] - x[2]*z[3]) / det;
    m[5]  = (x[1]*z[3] - x[3]*z[1]) / det;
    m[6]  = (z[1]*x[2] - x[1]*z[2]) / det;
    m[7]  = 0.0;

    m[8]  = (x[2]*y[3] - x[3]*y[2]) / det;
    m[9]  = (y[1]*x[3] - x[1]*y[3]) / det;
    m[10] = (x[1]*y[2] - y[1]*x[2]) / det;
    m[11] = 0.0;

    m[12] = 0.0;
    m[13] = 0.0;
    m[14] = 0.0;
    m[15] = 1.0;

    Matrix4x4 matrix = new Matrix4x4(m);
    multiply(matrix);

    // Scale according to height and width of viewport
    matrix.setIdentity();
    matrix.setElement(0, 0, width);
    matrix.setElement(1, 1, height);
    multiply(matrix);

    // Translate according to origin of viewport
    matrix.setIdentity();
    matrix.setElement(3, 0, x0);
    matrix.setElement(3, 1, y0);
    multiply(matrix);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    int hashCode = 1;
    for (int i = 0; i < 16; i++) {
      long v = Double.doubleToLongBits(m_[i]);
      int hashValue = (int) (v ^ (v >>> 32));
      hashCode = 31 * hashCode + hashValue;
    }

    return hashCode;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object object)
  {
    if (object == this)
      return true;

    if (object == null)
      return false;

    if (!(object instanceof Matrix4x4))
      return false;

    Matrix4x4 matrix = (Matrix4x4) object;

    for (int i = 0; i < 16; i++)
      if (m_[i] != matrix.m_[i])
        return false;

    return true;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();

    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++)
        s.append(getElement(i,j) + " ");
      s.append('\n');
    }

    return s.toString();
  }
}
