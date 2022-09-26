

# Turkish Phone Text Input Mask Library

This kotlin library can be useful to create mask for turkish phone numbers like
**(598) 76_ __ __**

## Demo
!!! Will add gif here !!!


## Installation

!!! Will add installation here !!!

## Usage

For the sample app you can run to root project in repository or you can follow instructions below.
### Set Up Layout File

You need set some Edittext attributes in your layout file.
```kotlin
<androidx.appcompat.widget.AppCompatEditText  
  android:id="@+id/et_defaultPhoneNumber"  
  android:layout_width="match_parent"  
  android:layout_height="wrap_content"  
  android:digits="0123456789"  
  android:inputType="phone"  
  android:maxLength="16"  
  android:singleLine="true"/>
```

> You must give least one more input length over mask to write input.

### Create variable for library

Variable declaration needed for getting unmasked phone number from instance.
```kotlin  
class MyActivity : AppCompatActivity() {  
 private lateinit var turkishPhoneMask:TurkishPhoneTextWatcher 
 ...  
```  

### Create and set instance to the variable

Library needs Edittext reference to apply mask on it. So you need to pass the binding as code below.
```kotlin  
override fun onCreate(savedInstanceState: Bundle?) {  
 super.onCreate(savedInstanceState) 
 turkishPhoneMask = TurkishPhoneTextWatcher(mainActivityBinding.etDefaultPhoneNumber) 
 ...  
```  
### (Optional) You can give prefix if you want to

In default we set default mask prefix to '_'(underline). But you can specify prefix with passing second parameter like this.  
There is no restriction for additional prefix. So you can pass UTF-16 characters, even number(but not recommended :D) .
```kotlin  
override fun onCreate(savedInstanceState: Bundle?) {  
 super.onCreate(savedInstanceState) 
 turkishPhoneMask = TurkishPhoneTextWatcher(mainActivityBinding.etDefaultPhoneNumber,'*') 
 ...  
```  
### To get phone when needed

You can get phone number from **phoneNumber** field. It return unmasked phone number from input. Ex: **5987654321**
```kotlin  
turkishPhoneMask.phoneNumber  
```  

## Contributing

Feel free to open issue and pull requests. We need to fix and improve this library. You can check to Todo tabs to may you can develop feature before us. It'll pleasure to collaborate.✌️

## License
[MIT](https://choosealicense.com/licenses/mit/)