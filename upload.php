<?php
$target_dir = "images/";
$file_name = uniqid();
$file_extension = pathinfo($_FILES["file"]["name"],PATHINFO_EXTENSION);
$target_file = $target_dir . $file_name . "." . $file_extension;
$uploadOk = 1;
$imageFileType = strtolower(pathinfo($target_file,PATHINFO_EXTENSION));

// Check if image file is a actual image or fake image
if(isset($_POST["submit"])) {
  $check = getimagesize($_FILES["file"]["tmp_name"]);
  if($check !== false) {
    $uploadOk = 1;
  } else {
    $uploadOk = 0;
  }
}

// Check if file already exists
if (file_exists($target_file)) {
  $uploadOk = 0;
}

// Check file size
if ($_FILES["file"]["size"] > 500000) {
  $uploadOk = 0;
}

// Allow certain file formats
if($imageFileType != "jpg" && $imageFileType != "png" && $imageFileType != "jpeg"
&& $imageFileType != "gif" ) {
  $uploadOk = 0;
}

// Check if $uploadOk is set to 0 by an error
if ($uploadOk == 0) {
  $result = array('success' => false);
  echo json_encode($result, JSON_FORCE_OBJECT);
// if everything is ok, try to upload file
} else {
  if (move_uploaded_file($_FILES["file"]["tmp_name"], $target_file)) {
    $result = array('success' => true, 'link' => 'https://thatsapp.kreienbuehl.dev/' . $target_file);
    echo json_encode($result, JSON_FORCE_OBJECT);
  } else {
    $result = array('success' => false);
    echo json_encode($result, JSON_FORCE_OBJECT);
  }
}
?>
