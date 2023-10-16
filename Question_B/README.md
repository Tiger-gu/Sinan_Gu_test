Summary:
   1. Similar to the Question A, I have used Java to implement the solution.
   2. Similar to the Question A, I have used Junit to write out all the test cases
      in the form of unit testing.

My considerations:

   1. Enforced a simple version format scheme which is considered universal: 
         digits.digits.digits ...
      For example, 2.1.15

      Enforcing a vesion format scheme that most people will take for granted since
      they've seen this kind of format for too many times can help to reduce the number of
      faults.

   2. Reasonable Faults tolerance.
      Since developers very often have comlete control
      over version formats, it makes sense to only build tolerance
      for the most common mistake.
      In my experience, the most common fault for version strings is: 
      extra whitespaces. Sometimes, when developers perform CRUD operations, 
      they will accidentally inject whitespaces into version strings such as " 1.2 .3.4 ". 

      The code I wrote will cover those mistakes. However, for other faults
      which stray away from the standard format will trigger an
      illegalArgumentException. Alternatively, we can also create custom exceptions
      if necessary.

   3. Scalability 
      I fancy that scalability sometimes is a problem for a company. 
      As a company grows, possibly it will not be satisfied with a three-part
      version string format such as 2.10.8. The company perhaps wants to expand
      the three-part string format into four-part ones. In this case, comparing
      old ones with new ones could become a problem. For example, the comparison
      between 15.18.19 and 15.18.19.1.

      The code I wrote will cover those comparisons so that developers do not need to
      update all of old version strings by padding 0 or other operations. 



   

   


