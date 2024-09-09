// Navbar Scroll Effect and Burger Menu Color Control


console.log("Hello JA")

window.onscroll = function() {
  const navbar = document.querySelector('.navbar');
  
  if (window.scrollY > 50) {
    navbar.classList.add('navbar-scrolled');
  } else if (!navbar.classList.contains('navbar-expanded')) {
    navbar.classList.remove('navbar-scrolled');
  }
};

// Select all navbar links and the navbar toggle button
const navLinks = document.querySelectorAll('.nav-link');
const navbarCollapse = document.querySelector('.navbar-collapse');
const navbarToggler = document.querySelector('.navbar-toggler');

// Function to toggle the burger icon (X to bars and vice versa)
function toggleBurgerIcon() {
  navbarToggler.classList.toggle('collapsed'); // Toggle the 'collapsed' class to reset the icon
}

// Loop through each nav-link
navLinks.forEach(link => {
  link.addEventListener('click', () => {
    // Collapse the navbar after clicking a link (only in mobile/tab view)
    if (navbarCollapse.classList.contains('show')) {
      new bootstrap.Collapse(navbarCollapse).toggle();
      toggleBurgerIcon(); // Switch the icon back to three bars after closing
    }
  });
});

// Event listener for toggling the icon when the navbar is opened/closed
navbarToggler.addEventListener('click', () => {
  toggleBurgerIcon();
});


// ##### Counter Design #####
document.addEventListener("DOMContentLoaded",() =>{
  function counter(id,start,end,duration){
      let obj = document.getElementById(id),
      current = start,
      range = end - start,
      increment = end > start ? 1 : -1,
      step = Math.abs(Math.floor(duration / range)),
      timer = setInterval(() =>{
          current += increment;
          obj.textContent = current;
          if(current == end){
              clearInterval(timer);
          }
      }, step);
  }
  counter("count1",0,50,10000);
  counter("count2",0,20,10000);
  counter("count3",0,35,10000);
  counter("count4",0,100,10000);
})




