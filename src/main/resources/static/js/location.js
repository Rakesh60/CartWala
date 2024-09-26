// Automatically get location on page load
window.addEventListener("load", function () {
  if (navigator.geolocation) {
    navigator.geolocation.getCurrentPosition(showPosition, showError);
  } else {
    document.getElementById("location").innerHTML =
      "Geolocation is not supported by this browser.";
  }
});

function showPosition(position) {
  const latitude = position.coords.latitude;
  const longitude = position.coords.longitude;

  // Fetch the city and pincode using the Nominatim API
  const apiUrl = `https://nominatim.openstreetmap.org/reverse?lat=${latitude}&lon=${longitude}&format=json`;

  fetch(apiUrl)
    .then((response) => response.json())
    .then((data) => {
      const city =
        data.address.city ||
        data.address.town ||
        data.address.village ||
        "City not found";
      const pincode = data.address.postcode || "Pincode not found";

      document.getElementById("location").innerHTML = `
                   <i class="text-warning fas fa-location-dot fs-7"></i> ${city}
                   `;
    })
    .catch((err) => {
      document.getElementById("location").innerHTML =
        "Unable to retrieve location information.";
    });
}

function showError(error) {
  switch (error.code) {
    case error.PERMISSION_DENIED:
      document.getElementById("location").innerHTML =
        "User denied the request for Geolocation.";
      break;
    case error.POSITION_UNAVAILABLE:
      document.getElementById("location").innerHTML =
        "Location information is unavailable.";
      break;
    case error.TIMEOUT:
      document.getElementById("location").innerHTML =
        "The request to get user location timed out.";
      break;
    case error.UNKNOWN_ERROR:
      document.getElementById("location").innerHTML =
        "An unknown error occurred.";
      break;
  }
}
