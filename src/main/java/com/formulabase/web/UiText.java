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
Map.entry("Production task rescheduled","เลื่อนกำหนดการผลิตแล้ว"),
Map.entry("Planning cycle locked","ล็อกแผนแล้ว"),
Map.entry("Formula submitted for CEO approval","ส่งสูตรให้ CEO อนุมัติแล้ว"),
Map.entry("Formula confirmed by CEO","CEO อนุมัติสูตรแล้ว"),
Map.entry("Invalid username or password","ชื่อผู้ใช้หรือรหัสผ่านไม่ถูกต้อง"),
Map.entry("Document number and formula name are required","กรอกเลขเอกสารและชื่อสูตร"),
Map.entry("Document number already exists","เลขเอกสารนี้มีอยู่แล้ว"),
Map.entry("Enter a reason for the formula revision","กรุณาระบุเหตุผลที่แก้ไขสูตร"),
Map.entry("At least one material row is required","เพิ่มวัตถุดิบอย่างน้อยหนึ่งรายการ"),
Map.entry("Select an existing material or fill in new material code, name and unit","เลือกวัตถุดิบเดิมหรือกรอกรหัส ชื่อ และหน่วยให้ครบ"),
Map.entry("Invalid material unit","หน่วยวัตถุดิบไม่ถูกต้อง"),
Map.entry("Only draft or rejected formulas can be submitted","ส่งได้เฉพาะสูตรฉบับร่างหรือสูตรที่ไม่อนุมัติ"),
Map.entry("Only draft or pending formulas can be confirmed","อนุมัติได้เฉพาะสูตรฉบับร่างหรือสูตรรออนุมัติ"),
Map.entry("Only submitted formulas can be confirmed","อนุมัติได้เฉพาะสูตรที่ส่งให้อนุมัติแล้ว"),
Map.entry("Password must contain at least 10 characters","รหัสผ่านต้องมีอย่างน้อย 10 ตัวอักษร"),
Map.entry("Target time must be greater than zero","เวลาเป้าหมายต้องมากกว่า 0"),
Map.entry("Target time is too large","เวลาที่กำหนดมากเกินไป กรุณากรอกเวลาให้น้อยลง"),
Map.entry("Please sign in first","กรุณาเข้าสู่ระบบ"),
Map.entry("You do not have permission for this action","คุณไม่มีสิทธิ์ดำเนินการนี้"),
Map.entry("Select a valid planning month","เลือกเดือนให้ถูกต้อง"),
Map.entry("Planning cycle already exists for this month","มีแผนของเดือนนี้แล้ว"),
Map.entry("Locked planning cycle cannot be edited","แผนถูกล็อกแล้ว"),
Map.entry("Only confirmed formulas can be planned","วางแผนได้เฉพาะสูตรที่อนุมัติแล้ว"),
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
Map.entry("Assigned user must have WORKER role","ต้องมอบหมายงานให้พนักงานผลิต"),
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
Map.entry("QC must set a temperature limit before this step can be completed","QC ต้องกำหนดเกณฑ์อุณหภูมิของขั้นตอนนี้ก่อนจบงาน"),
Map.entry("QC can edit only a formula they created","QC แก้ไขได้เฉพาะสูตรที่ตนเองสร้าง"),
Map.entry("Only active steps can be completed","บันทึกจบได้เฉพาะขั้นตอนที่กำลังทำอยู่"),
Map.entry("Complete earlier steps first","ต้องทำขั้นตอนก่อนหน้าให้เสร็จก่อน"),
Map.entry("Start the batch before a production step","กรุณากดเริ่มผลิตงานนี้ก่อนเริ่มขั้นตอน"),
Map.entry("Production step not found","ไม่พบขั้นตอนการผลิต"),
Map.entry("Reschedule reason is required","กรุณาระบุเหตุผลการเลื่อนแผน"),
Map.entry("Only planned batches can be cancelled","ยกเลิกได้เฉพาะงานที่ยังไม่เริ่มผลิต"),
Map.entry("Only planned tasks can be cancelled","ยกเลิกได้เฉพาะงานที่ยังไม่เริ่มผลิต"),
Map.entry("Worker not found","ไม่พบผู้ผลิต"),
Map.entry("Quality review saved","บันทึกการตรวจคุณภาพแล้ว"),
Map.entry("Formula updated","บันทึกการแก้ไขสูตรแล้ว"),
Map.entry("Formula rejected","ไม่อนุมัติสูตรแล้ว"),
Map.entry("Formula deleted","ลบสูตรแล้ว"),
Map.entry("Production task cancelled","ยกเลิกงานผลิตแล้ว"),
Map.entry("Material deleted","ลบวัตถุดิบแล้ว"),
Map.entry("Production step started","เริ่มขั้นตอนการผลิตแล้ว"),
Map.entry("Production step recorded","บันทึกขั้นตอนการผลิตแล้ว"),
Map.entry("Overdue alert acknowledged","รับทราบการแจ้งเตือนเกินเวลาแล้ว"),
Map.entry("User created","สร้างผู้ใช้แล้ว"),
Map.entry("User deleted","ลบบัญชีผู้ใช้แล้ว"),
Map.entry("User removed; history retained","นำบัญชีออกแล้ว โดยเก็บประวัติการทำงานไว้"),
Map.entry("Choose a replacement for every unfinished task","กรุณาเลือกพนักงานรับช่วงให้ครบทุกงานที่ยังไม่เสร็จ"),
Map.entry("Select an active replacement worker","กรุณาเลือกพนักงานผลิตที่ยังใช้งานได้และไม่ใช่บัญชีที่กำลังนำออก"),
Map.entry("Tasks changed; review the list and try again","ข้อมูลงานเปลี่ยนแล้ว กรุณาตรวจสอบรายการอีกครั้ง"),
Map.entry("Username belongs to a removed account","ชื่อผู้ใช้นี้เคยใช้กับบัญชีที่นำออกแล้ว กรุณาใช้ชื่ออื่น"),
Map.entry("Cannot delete your own account","ไม่สามารถลบบัญชีที่กำลังใช้งานอยู่ได้"),
Map.entry("User has related records; deactivate instead","บัญชีนี้มีข้อมูลหรือประวัติที่เกี่ยวข้อง จึงลบไม่ได้ กรุณาใช้ปุ่มปิดใช้งานแทน"),
Map.entry("User not found","ไม่พบบัญชีผู้ใช้"),
Map.entry("User status updated","อัปเดตสถานะผู้ใช้แล้ว"),
Map.entry("Only CEO can manage users","เฉพาะ CEO เท่านั้นที่จัดการผู้ใช้ได้"),
Map.entry("Complete all user fields","กรอกข้อมูลผู้ใช้ให้ครบ"),
Map.entry("Username already exists","ชื่อผู้ใช้นี้มีอยู่แล้ว"),
Map.entry("CEO cannot deactivate the current account","CEO ไม่สามารถปิดใช้งานบัญชีที่กำลังใช้ได้"),
Map.entry("Rescheduling closes at 23:59:59 on the day before production","เลื่อนกำหนดการได้ถึงเวลา 23:59:59 ของวันก่อนวันผลิต"),
Map.entry("Only CEO or PC can cancel a plan","เฉพาะ CEO หรือ PC เท่านั้นที่ยกเลิกงานผลิตได้"),
Map.entry("Only CEO can change a locked month","เฉพาะ CEO เท่านั้นที่เปลี่ยนแผนเดือนที่ล็อกแล้วได้"),
Map.entry("Material is used in a formula and cannot be deleted","วัตถุดิบนี้ถูกใช้ในสูตร จึงลบไม่ได้"),
Map.entry("You do not have permission to print a BOM","คุณไม่มีสิทธิ์พิมพ์ BOM"),
Map.entry("You do not have permission to print this work order","คุณไม่มีสิทธิ์พิมพ์ใบสั่งผลิตนี้"),
Map.entry("Enter a quantity per product unit for every material","กรอกปริมาณต่อชิ้นให้ครบทุกวัตถุดิบ"),
Map.entry("A material can be added only once per formula","วัตถุดิบหนึ่งรายการเพิ่มในสูตรได้เพียงครั้งเดียว"),
Map.entry("Enter both Thai and English production instructions for every step","กรอกคำสั่งผลิตภาษาไทยและอังกฤษให้ครบทุกขั้นตอน"),
Map.entry("Set an acceptable minimum or maximum temperature for every temperature-required step","กำหนดอุณหภูมิขั้นต่ำหรือสูงสุดสำหรับทุกขั้นตอนที่ต้องบันทึกอุณหภูมิ"),
Map.entry("Temperature limits require the temperature-required option","ต้องเลือกให้ขั้นตอนบันทึกอุณหภูมิก่อนจึงกำหนดเกณฑ์อุณหภูมิได้"),
Map.entry("Minimum temperature cannot exceed maximum temperature","อุณหภูมิขั้นต่ำต้องไม่สูงกว่าอุณหภูมิสูงสุด"),
Map.entry("Invalid material phase","เฟสวัตถุดิบไม่ถูกต้อง"),
Map.entry("Enter a finished-product weight in grams for each piece","กรอกน้ำหนักสินค้าสำเร็จรูปต่อชิ้นเป็นกรัม"),
Map.entry("Enter a non-negative % w/w value for every material","กรอกค่า % w/w ที่ไม่ติดลบให้ครบทุกวัตถุดิบ"),
Map.entry("Formula percentages must total 100% w/w","เปอร์เซ็นต์ของสูตรต้องรวมเป็น 100% w/w"),
Map.entry("CEO self-approval requires a reason","CEO ต้องระบุเหตุผลเมื่ออนุมัติสูตรของตนเอง"),
Map.entry("Only confirmed formulas can be revised","แก้ไขรุ่นใหม่ได้เฉพาะสูตรที่อนุมัติแล้ว"),
Map.entry("Rejection reason is required","กรุณาระบุเหตุผลที่ไม่อนุมัติ"),
Map.entry("Only pending formulas can be rejected","ไม่อนุมัติได้เฉพาะสูตรที่รออนุมัติ"),
Map.entry("Only draft or rejected formulas can be edited","แก้ไขได้เฉพาะสูตรฉบับร่างหรือสูตรที่ไม่อนุมัติ"),
Map.entry("Only draft or rejected formulas can be deleted","ลบได้เฉพาะสูตรฉบับร่างหรือสูตรที่ไม่อนุมัติ"),
Map.entry("Material phase is required","กรุณาเลือกเฟสวัตถุดิบ"),
Map.entry("Material percentage cannot be negative","เปอร์เซ็นต์วัตถุดิบห้ามติดลบ"),
Map.entry("Review is not available","รายการนี้อาจตรวจแล้วหรือไม่อยู่ในสถานะรอตรวจ กรุณาโหลดหน้าใหม่"),
Map.entry("Enter a review note (1-255 characters)","กรอกหมายเหตุการตรวจ 1–255 ตัวอักษร"),
Map.entry("Only pending steps can be started","เริ่มได้เฉพาะขั้นตอนที่รอเริ่ม"),
Map.entry("This step has no overdue alert","ขั้นตอนนี้ไม่มีการแจ้งเตือนเกินเวลา"),
Map.entry("Enter an acknowledgement note","กรุณากรอกข้อความรับทราบ"),
Map.entry("Only CEO or PC can delete draft plans","เฉพาะ CEO หรือ PC เท่านั้นที่ลบแผนฉบับร่างได้"),
Map.entry("Only draft monthly plans can be deleted","ลบได้เฉพาะแผนรายเดือนฉบับร่าง แผนที่ล็อกแล้วต้องเก็บไว้เป็นประวัติ"),
Map.entry("Cannot delete a task with production or schedule history","ลบไม่ได้ เพราะงานนี้มีประวัติการผลิตหรือประวัติเลื่อนกำหนด"),
Map.entry("Draft production task deleted","ลบงานผลิตฉบับร่างแล้ว"),
Map.entry("Draft monthly plan deleted","ลบแผนรายเดือนฉบับร่างแล้ว"),
Map.entry("Action could not be completed","ไม่สามารถดำเนินการได้"),
Map.entry("Access denied","ไม่มีสิทธิ์เข้าถึง"),
Map.entry("Something went wrong","เกิดข้อผิดพลาด"),
Map.entry("The request could not be completed. Please return to the application and try again.","ไม่สามารถดำเนินการตามคำขอได้ กรุณากลับไปที่ระบบและลองอีกครั้ง"));
 public String translate(String text) {
 if(text==null)return "";
 if(!"th".equals(LocaleContextHolder.getLocale().getLanguage()))return switch(text) {
  case "Formula confirmed by CEO" -> "Formula approved by CEO";
  case "Formula rejected" -> "Formula not approved";
  case "Review is not available" -> "This task may already be reviewed or no longer awaiting review. Please refresh the page.";
  default -> text;
 };
 if(text.startsWith("Formula saved: "))return "บันทึกสูตรแล้ว: "+text.substring(15);
 if(text.startsWith("Created revision "))return "สร้างฉบับแก้ไขแล้ว: "+text.substring("Created revision ".length());
 if(text.startsWith("Batch size must be between "))return "จำนวนผลิตต้องอยู่ระหว่าง "+text.substring("Batch size must be between ".length());
 if(text.startsWith("Temperature must be at least "))return "อุณหภูมิต้องไม่น้อยกว่า "+text.substring("Temperature must be at least ".length());
 if(text.startsWith("Temperature must not exceed "))return "อุณหภูมิต้องไม่เกิน "+text.substring("Temperature must not exceed ".length());
 return TH.getOrDefault(text,"ไม่สามารถดำเนินการได้ กรุณาตรวจข้อมูลที่กรอก");
 }

 public String auditAction(String action, String language) {
  if (action == null) return "";
  if (!"th".equals(language)) return switch (action) {
   case "CREATE_FORMULA" -> "Create formula"; case "SUBMIT_FORMULA" -> "Submit formula for approval";
   case "CONFIRM_FORMULA" -> "Approve formula"; case "CREATE_FORMULA_REVISION" -> "Create formula revision";
   case "REJECT_FORMULA" -> "Reject formula"; case "EDIT_FORMULA" -> "Edit formula"; case "DELETE_FORMULA" -> "Delete formula";
   case "CREATE_MATERIAL" -> "Add material"; case "DELETE_MATERIAL" -> "Delete material";
   case "DELETE_PLANNING_CYCLE" -> "Delete draft monthly plan"; case "DELETE_PRODUCTION_PLAN" -> "Delete draft production task";
   case "CREATE_PLANNING_CYCLE" -> "Create monthly plan"; case "CREATE_PRODUCTION_PLAN" -> "Add production task";
   case "LOCK_PLANNING_CYCLE" -> "Lock monthly plan"; case "RESCHEDULE_PLAN" -> "Edit production schedule";
   case "CANCEL_PLAN" -> "Cancel production task"; case "START_BATCH" -> "Start batch";
   case "START_STEP" -> "Start production step"; case "COMPLETE_STEP" -> "Complete production step";
   case "COMPLETE_STEP_OVERDUE" -> "Complete step after target time"; case "ACKNOWLEDGE_OVERDUE" -> "Acknowledge overdue alert";
   case "COMPLETE_BATCH" -> "Record batch output"; case "QUALITY_REVIEW" -> "Review batch quality";
   case "CREATE_USER" -> "Create user"; case "DELETE_USER" -> "Delete user"; case "TOGGLE_USER" -> "Change user status"; case "TRANSFER_WORK" -> "Transfer production task";
   case "OPEN_BOM_PRINT_PREVIEW" -> "Open BOM print preview"; case "OPEN_WORK_ORDER_PRINT_PREVIEW" -> "Open work order print preview";
   default -> action.replace('_', ' ').toLowerCase(java.util.Locale.ROOT);
  };
  return switch (action) {
   case "CREATE_FORMULA" -> "สร้างสูตร"; case "SUBMIT_FORMULA" -> "ส่งสูตรให้อนุมัติ";
   case "CONFIRM_FORMULA" -> "อนุมัติสูตร"; case "CREATE_FORMULA_REVISION" -> "สร้างฉบับแก้ไขสูตร";
   case "REJECT_FORMULA" -> "ไม่อนุมัติสูตร"; case "EDIT_FORMULA" -> "แก้ไขสูตร"; case "DELETE_FORMULA" -> "ลบสูตร";
   case "CREATE_MATERIAL" -> "เพิ่มวัตถุดิบ"; case "DELETE_MATERIAL" -> "ลบวัตถุดิบ";
   case "DELETE_PLANNING_CYCLE" -> "ลบแผนรายเดือนฉบับร่าง"; case "DELETE_PRODUCTION_PLAN" -> "ลบงานผลิตฉบับร่าง";
   case "CREATE_PLANNING_CYCLE" -> "สร้างแผนรายเดือน"; case "CREATE_PRODUCTION_PLAN" -> "เพิ่มงานผลิต";
   case "LOCK_PLANNING_CYCLE" -> "ล็อกแผนรายเดือน"; case "RESCHEDULE_PLAN" -> "แก้ไขกำหนดการผลิต";
   case "CANCEL_PLAN" -> "ยกเลิกงานผลิต"; case "START_BATCH" -> "เริ่มงานผลิต";
   case "START_STEP" -> "เริ่มขั้นตอนผลิต"; case "COMPLETE_STEP" -> "จบขั้นตอนผลิต";
   case "COMPLETE_STEP_OVERDUE" -> "จบขั้นตอนหลังเกินเวลา"; case "ACKNOWLEDGE_OVERDUE" -> "รับทราบแจ้งเตือนเกินเวลา";
   case "COMPLETE_BATCH" -> "บันทึกผลผลิต"; case "QUALITY_REVIEW" -> "ตรวจสอบคุณภาพงานผลิต";
   case "CREATE_USER" -> "สร้างผู้ใช้"; case "DELETE_USER" -> "ลบผู้ใช้"; case "TOGGLE_USER" -> "เปลี่ยนสถานะผู้ใช้"; case "TRANSFER_WORK" -> "ส่งต่องานผลิต";
   case "OPEN_BOM_PRINT_PREVIEW" -> "เปิดตัวอย่างพิมพ์ BOM"; case "OPEN_WORK_ORDER_PRINT_PREVIEW" -> "เปิดตัวอย่างพิมพ์ใบสั่งผลิต";
   default -> action.replace('_', ' ').toLowerCase(java.util.Locale.ROOT);
  };
 }

 public String auditSubject(String subject, String language) {
  if (subject == null) return "";
  if (!"th".equals(language)) return switch (subject) { case "FORMULA" -> "Formula"; case "PLAN" -> "Production task"; case "STEP" -> "Production step"; case "CYCLE" -> "Monthly plan"; case "MATERIAL" -> "Material"; case "USER" -> "User"; default -> subject; };
  return switch (subject) { case "FORMULA" -> "สูตร"; case "PLAN" -> "งานผลิต"; case "STEP" -> "ขั้นตอนผลิต"; case "CYCLE" -> "แผนรายเดือน"; case "MATERIAL" -> "วัตถุดิบ"; case "USER" -> "ผู้ใช้"; default -> subject; };
 }
 public String auditDetails(String details, String language) {
  if (details == null || !"th".equals(language)) return details;
  String result = details;
  String[][] terms = {{"BOM basis ", "คำนวณ BOM สำหรับ "}, {" product unit(s)", " ชิ้น"},
   {"Opened work order for ", "เปิดใบสั่งผลิตวันที่ "}, {"Created task for ", "เพิ่มงานผลิตวันที่ "},
   {"Submitted ", "ส่ง "}, {" for approval", " เพื่ออนุมัติ"}, {"Confirmed ", "อนุมัติ "},
   {"Created formula ", "สร้างสูตร "}, {"Revision ", "สูตรรุ่น "}, {" derived from ", " สร้างจาก "},
   {"Deleted ", "ลบ "}, {"Loss allowance ", "จำนวนเผื่อของเสีย "},
   {"Actual ", "ผลิตจริง "}, {", rejected ", ", จำนวนเสีย "},
   {"Created ", "สร้าง "}, {"Locked ", "ล็อก "}, {"Cancelled task on ", "ยกเลิกงานวันที่ "},
   {"User @", "ผู้ใช้ @"}, {" active=true", " ใช้งาน"}, {" active=false", " ปิดใช้งาน"},
   {"; worker unassigned", "; ยังไม่มอบหมายพนักงาน"}, {"; worker ", "; พนักงาน "}};
  for (String[] term : terms) result = result.replace(term[0], term[1]);
  return result;
 }

}
