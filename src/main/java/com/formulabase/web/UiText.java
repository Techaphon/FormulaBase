package com.formulabase.web;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.context.i18n.LocaleContextHolder;
@Component("uiText")
public class UiText {
 private static final Map<String,String> TH = Map.ofEntries(
Map.entry("Batch started","เริ่มผลิตแล้ว"),
Map.entry("Batch recorded","บันทึกผลการผลิตแล้ว"),
Map.entry("Material saved","บันทึกวัตถุดิบแล้ว"),
Map.entry("Planning cycle created","สร้างแผนรายเดือนแล้ว"),
Map.entry("Production task added","เพิ่มงานผลิตแล้ว"),
Map.entry("Planning cycle locked","ล็อกแผนแล้ว"),
Map.entry("Formula submitted for CEO approval","ส่งสูตรให้ CEO ยืนยันแล้ว"),
Map.entry("Formula confirmed by CEO","CEO ยืนยันสูตรแล้ว"),
Map.entry("Invalid username or password","ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง"),
Map.entry("Document number and formula name are required","กรอกเลขเอกสารและชื่อสูตร"),
Map.entry("Document number already exists","เลขเอกสารนี้มีอยู่แล้ว"),
Map.entry("At least one material row is required","เพิ่มวัตถุดิบอย่างน้อยหนึ่งรายการ"),
Map.entry("Select an existing material or fill in new material code, name and unit","เลือกวัตถุดิบเดิมหรือกรอกรหัส ชื่อ และหน่วยให้ครบ"),
Map.entry("Invalid material unit","หน่วยวัตถุดิบไม่ถูกต้อง"),
Map.entry("Only draft or rejected formulas can be submitted","ส่งได้เฉพาะสูตรฉบับร่างหรือสูตรที่ไม่อนุมัติ"),
Map.entry("Only draft or pending formulas can be confirmed","ยืนยันได้เฉพาะสูตรฉบับร่างหรือสูตรรอยืนยัน"),
Map.entry("Only submitted formulas can be confirmed","ยืนยันได้เฉพาะสูตรที่ส่งให้อนุมัติแล้ว"),
Map.entry("Password must contain at least 10 characters","รหัสผ่านต้องมีอย่างน้อย 10 ตัวอักษร"),
Map.entry("Target time must be greater than zero","เวลาเป้าหมายต้องมากกว่า 0"),
Map.entry("Please sign in first","กรุณาเข้าสู่ระบบ"),
Map.entry("You do not have permission for this action","คุณไม่มีสิทธิ์ดำเนินการนี้"),
Map.entry("Select a valid planning month","เลือกเดือนให้ถูกต้อง"),
Map.entry("Planning cycle already exists for this month","มีแผนของเดือนนี้แล้ว"),
Map.entry("Locked planning cycle cannot be edited","แผนถูกล็อกแล้ว"),
Map.entry("Only confirmed formulas can be planned","วางแผนได้เฉพาะสูตรที่ยืนยันแล้ว"),
Map.entry("Planned date must be inside the selected month","วันผลิตต้องอยู่ในเดือนที่เลือก"),
Map.entry("Target quantity must be greater than zero","จำนวนเป้าหมายต้องมากกว่าศูนย์"),
Map.entry("Planned date is required","กรุณาระบุวันผลิต"),
Map.entry("Add at least one production task before locking the month","เพิ่มงานผลิตอย่างน้อยหนึ่งงานก่อนล็อกแผน"),
Map.entry("Only CEO or PC can reschedule a plan","เฉพาะ CEO หรือ PC เท่านั้นที่เลื่อนแผนได้"),
Map.entry("New date must be within the planning month","วันใหม่ต้องอยู่ในเดือนของแผน"),
Map.entry("Only workers can view worker tasks","ไม่มีสิทธิ์ดูงานผลิต"),
Map.entry("Lock the monthly plan before starting production","กรุณาล็อกแผนรายเดือนก่อนเริ่มผลิต"),
Map.entry("This task is scheduled for a future date","ยังไม่ถึงวันผลิตของงานนี้"),
Map.entry("Loss percentage is required before Start","กรอกเปอร์เซ็นต์เผื่อการสูญเสียก่อนเริ่ม"),
Map.entry("This batch is not assigned to the current worker","งานนี้ไม่ได้มอบหมายให้คุณ"),
Map.entry("Target quantity cannot be negative","จำนวนเป้าหมายห้ามติดลบ"),
Map.entry("Loss percentage must be between 0 and 100","เปอร์เซ็นต์เผื่อการสูญเสียต้องอยู่ระหว่าง 0 ถึง 100"),
Map.entry("Code, name and unit are required","กรอกรหัส ชื่อ และหน่วย"),
Map.entry("Material code already exists","รหัสวัตถุดิบนี้มีอยู่แล้ว"),
Map.entry("Quantity per unit cannot be negative","ปริมาณต่อชิ้นห้ามติดลบ"),
Map.entry("Planning month must use yyyy-MM format","เดือนต้องเป็นรูปแบบ ปี-เดือน"),
Map.entry("Assigned user must have WORKER role","ต้องมอบหมายให้ผู้ใช้บทบาท Worker"),
Map.entry("Only planned batches can be started","เริ่มได้เฉพาะงานที่ยังไม่เริ่มผลิต"),
Map.entry("Only planned tasks can be rescheduled","เลื่อนได้เฉพาะงานที่ยังไม่เริ่มผลิต"),
Map.entry("Only in-progress batches can be completed","บันทึกผลได้เฉพาะงานที่กำลังผลิต"),
Map.entry("Production quantities are invalid","จำนวนผลิตหรือจำนวนเสียไม่ถูกต้อง"),
Map.entry("Material not found","ไม่พบวัตถุดิบ"),
Map.entry("Formula not found","ไม่พบสูตร"),
Map.entry("Planning cycle not found","ไม่พบแผนรายเดือน"),
Map.entry("Production plan not found","ไม่พบงานผลิต"),
Map.entry("Complete every production step before recording the final batch output","ต้องทำทุกขั้นตอนการผลิตให้ครบก่อนบันทึกผลผลิต"),
Map.entry("Temperature is required for this step","ขั้นตอนนี้ต้องบันทึกอุณหภูมิ"),
Map.entry("Only active steps can be completed","บันทึกจบได้เฉพาะขั้นตอนที่กำลังทำอยู่"),
Map.entry("Complete earlier steps first","ต้องทำขั้นตอนก่อนหน้าให้เสร็จก่อน"),
Map.entry("Start the batch before a production step","ต้องเริ่มผลิตทั้ง Batch ก่อนทำขั้นตอน"),
Map.entry("Production step not found","ไม่พบขั้นตอนการผลิต"),
Map.entry("Reschedule reason is required","กรุณาระบุเหตุผลการเลื่อนแผน"),
Map.entry("Only planned batches can be cancelled","ยกเลิกได้เฉพาะงานที่ยังไม่เริ่มผลิต"),
Map.entry("Worker not found","ไม่พบผู้ผลิต"));
 public String translate(String text) {
 if(text==null)return "";
 if(!"th".equals(LocaleContextHolder.getLocale().getLanguage()))return text;
 if(text.startsWith("Formula saved: "))return "บันทึกสูตรแล้ว: "+text.substring(15);
 return TH.getOrDefault(text,"ไม่สามารถดำเนินการได้ กรุณาตรวจข้อมูลที่กรอก");
 }
}
